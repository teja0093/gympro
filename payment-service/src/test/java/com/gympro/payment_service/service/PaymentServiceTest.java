package com.gympro.payment_service.service;

import com.gympro.payment_service.client.MemberClient;
import com.gympro.payment_service.client.NotificationClient;
import com.gympro.payment_service.dto.OrderResponse;
import com.gympro.payment_service.dto.PlanDto;
import com.gympro.payment_service.dto.VerifyRequest;
import com.gympro.payment_service.entity.Payment;
import com.gympro.payment_service.entity.PaymentStatus;
import com.gympro.payment_service.repository.PaymentRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private MemberClient memberClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        // the @Value fields are not filled by Mockito, so set them by hand
        ReflectionTestUtils.setField(paymentService, "mock", true);
        ReflectionTestUtils.setField(paymentService, "keyId", "test_key");
        ReflectionTestUtils.setField(paymentService, "keySecret", "test_secret");
        ReflectionTestUtils.setField(paymentService, "internalKey", "internal-key");
    }

    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/plans/1",
                Map.<String, Collection<String>>of(), null, StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, Map.of());
    }

    private Payment createdPayment() {
        return Payment.builder().memberEmail("naveen@gmail.com").planId(2L)
                .razorpayOrderId("order_1").status(PaymentStatus.CREATED).build();
    }

    private VerifyRequest verifyRequest(String signature) {
        VerifyRequest r = new VerifyRequest();
        r.setOrderId("order_1");
        r.setPaymentId("pay_1");
        r.setSignature(signature);
        return r;
    }

    // ---------- createOrder ----------

    @Test
    void createOrder_mockMode_savesCreatedPaymentAndReturnsMockOrder() {
        PlanDto plan = new PlanDto();
        plan.setId(2L);
        plan.setPrice(new BigDecimal("999"));
        when(memberClient.getPlan(2L)).thenReturn(plan);

        OrderResponse res = paymentService.createOrder("naveen@gmail.com", 2L);

        assertTrue(res.getOrderId().startsWith("order_mock_"));
        assertEquals(99900L, res.getAmountInPaise());      // 999 rupees = 99900 paise
        assertEquals("INR", res.getCurrency());

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertEquals(PaymentStatus.CREATED, captor.getValue().getStatus());
        assertEquals("naveen@gmail.com", captor.getValue().getMemberEmail());
    }

    @Test
    void createOrder_planNotFound_isNotFound() {
        when(memberClient.getPlan(9L)).thenThrow(notFound());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.createOrder("naveen@gmail.com", 9L));

        assertEquals(404, ex.getStatusCode().value());
        verify(paymentRepository, never()).save(any());
    }

    // ---------- verify ----------

    @Test
    void verify_correctSignature_marksSuccessAndActivatesSubscription() {
        Payment payment = createdPayment();
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.of(payment));

        Payment result = paymentService.verify("naveen@gmail.com", verifyRequest("mock_success"));

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals("pay_1", result.getRazorpayPaymentId());
        verify(memberClient).subscribe("naveen@gmail.com", "internal-key", 2L);
    }

    @Test
    void verify_wrongSignature_marksFailedAndDoesNotSubscribe() {
        Payment payment = createdPayment();
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.of(payment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.verify("naveen@gmail.com", verifyRequest("wrong")));

        assertEquals(400, ex.getStatusCode().value());
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        verify(memberClient, never()).subscribe(any(), any(), any());
    }

    @Test
    void verify_orderBelongsToAnotherUser_isForbidden() {
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.of(createdPayment()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.verify("someone-else@gmail.com", verifyRequest("mock_success")));

        assertEquals(403, ex.getStatusCode().value());
        verify(memberClient, never()).subscribe(any(), any(), any());
    }

    @Test
    void verify_alreadyVerified_isConflict() {
        Payment payment = createdPayment();
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.of(payment));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.verify("naveen@gmail.com", verifyRequest("mock_success")));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void verify_unknownOrder_isNotFound() {
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.verify("naveen@gmail.com", verifyRequest("mock_success")));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void verify_activationFails_returnsBadGateway() {
        when(paymentRepository.findByRazorpayOrderId("order_1")).thenReturn(Optional.of(createdPayment()));
        when(memberClient.subscribe("naveen@gmail.com", "internal-key", 2L)).thenThrow(notFound());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.verify("naveen@gmail.com", verifyRequest("mock_success")));

        assertEquals(502, ex.getStatusCode().value());
    }
}