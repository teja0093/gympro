package com.gympro.notification_service.service;

import com.gympro.notification_service.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    // Mock mode: the "email" is written to the log.
    // Later this method can call Spring Mail instead.
    public void send(NotificationRequest req) {
        log.info("EMAIL SENT -> to: {} | subject: {} | message: {}",
                req.getTo(), req.getSubject(), req.getMessage());
    }
}