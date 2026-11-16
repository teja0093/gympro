package com.gympro.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private long amountInPaise;
    private String currency;
    private String keyId;
}