package com.gympro.notification_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank @Email private String to;
    @NotBlank private String subject;
    @NotBlank private String message;
}