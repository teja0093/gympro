package com.gympro.payment_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PlanDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private int durationDays;
}