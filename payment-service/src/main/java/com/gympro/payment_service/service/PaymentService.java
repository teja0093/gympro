package com.gympro.payment_service.service;

import com.gympro.payment_service.client.MemberClient;
import com.gympro.payment_service.dto.*;
import com.gympro.payment_service.entity.Payment;
import com.gympro.payment_service.entity.PaymentStatus;
import com.gympro.payment_service.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberClient memberClient;

    @Value("${razorpay.mock:false}")
    private boolean mock;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${internal.key}")
    private String internalKey;

    public OrderResponse createOrder(String email, Long planId) {
        PlanDto plan;
        try {
            memberClient.getMe(email);               // member must have a profile
            plan = memberClient.getPlan(planId);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Plan not found, or you have not created your member profile yet");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Membership service is unavailable, try again later");
        }

        long paise = plan.getPrice().multiply(BigDecimal.valueOf(100)).longValue();

        // ---- Mock mode: no Razorpay needed ----
        if (mock) {
            String orderId = "order_mock_" + UUID.randomUUID().toString().substring(0, 8);
            paymentRepository.save(Payment.builder()
                    .memberEmail(email)
                    .planId(planId)
                    .amount(plan.getPrice())
                    .razorpayOrderId(orderId)
                    .status(PaymentStatus.CREATED)
                    .createdAt(LocalDateTime.now())
                    .build());
            return new OrderResponse(orderId, paise, "INR", "mock_key");
        }

        // ---- Real Razorpay ----
        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            JSONObject options = new JSONObject();
            options.put("amount", paise);
            options.put("currency", "INR");
            options.put("receipt", "plan_" + planId + "_" + System.currentTimeMillis());
            Order order = client.orders.create(options);
            String orderId = order.get("id");

            paymentRepository.save(Payment.builder()
                    .memberEmail(email)
                    .planId(planId)
                    .amount(plan.getPrice())
                    .razorpayOrderId(orderId)
                    .status(PaymentStatus.CREATED)
                    .createdAt(LocalDateTime.now())
                    .build());

            return new OrderResponse(orderId, paise, "INR", keyId);
        } catch (RazorpayException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not create the payment order: " + e.getMessage());
        }
    }

    public Payment verify(String email, VerifyRequest req) {
        Payment payment = paymentRepository.findByRazorpayOrderId(req.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!payment.getMemberEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order belongs to another user");
        }
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment already verified");
        }

        boolean valid;
        if (mock) {
            valid = "mock_success".equals(req.getSignature());
        } else {
            try {
                JSONObject attrs = new JSONObject();
                attrs.put("razorpay_order_id", req.getOrderId());
                attrs.put("razorpay_payment_id", req.getPaymentId());
                attrs.put("razorpay_signature", req.getSignature());
                valid = Utils.verifyPaymentSignature(attrs, keySecret);
            } catch (RazorpayException e) {
                valid = false;
            }
        }

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment verification failed");
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(req.getPaymentId());
        paymentRepository.save(payment);

        try {
            memberClient.subscribe(email, internalKey, payment.getPlanId());
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Payment received but the subscription could not be activated. Contact support.");
        }
        return payment;
    }

    public List<Payment> myPayments(String email) {
        return paymentRepository.findByMemberEmailOrderByCreatedAtDesc(email);
    }

    public List<Payment> allPayments() {
        return paymentRepository.findAll();
    }
}