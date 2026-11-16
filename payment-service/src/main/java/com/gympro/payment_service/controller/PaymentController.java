package com.gympro.payment_service.controller;

import com.gympro.payment_service.dto.*;
import com.gympro.payment_service.entity.Payment;
import com.gympro.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public OrderResponse createOrder(@RequestHeader("X-User-Email") String email,
                                     @Valid @RequestBody CreateOrderRequest req) {
        return paymentService.createOrder(email, req.getPlanId());
    }

    @PostMapping("/verify")
    public Payment verify(@RequestHeader("X-User-Email") String email,
                          @Valid @RequestBody VerifyRequest req) {
        return paymentService.verify(email, req);
    }

    @GetMapping("/me")
    public List<Payment> mine(@RequestHeader("X-User-Email") String email) {
        return paymentService.myPayments(email);
    }

    @GetMapping
    public List<Payment> all(@RequestHeader("X-User-Role") String role) {
        if (!role.equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission");
        }
        return paymentService.allPayments();
    }
}