package com.gympro.attendance_service.dto;

import lombok.Data;

@Data
public class SubscriptionDto {
    private Long id;
    private String status;
    private String endDate;
}