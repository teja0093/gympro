package com.gympro.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyRequest {
    @NotBlank private String orderId;
    @NotBlank private String paymentId;
    @NotBlank private String signature;
}