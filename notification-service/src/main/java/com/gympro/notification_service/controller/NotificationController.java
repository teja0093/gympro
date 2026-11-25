package com.gympro.notification_service.controller;

import com.gympro.notification_service.dto.NotificationRequest;
import com.gympro.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public Map<String, String> send(@Valid @RequestBody NotificationRequest req) {
        notificationService.send(req);
        return Map.of("status", "SENT");
    }
}