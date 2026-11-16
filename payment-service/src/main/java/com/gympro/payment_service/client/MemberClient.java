package com.gympro.payment_service.client;

import com.gympro.payment_service.dto.PlanDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "member-service")
public interface MemberClient {

    @GetMapping("/plans/{id}")
    PlanDto getPlan(@PathVariable("id") Long id);

    @GetMapping("/members/me")
    Map<String, Object> getMe(@RequestHeader("X-User-Email") String email);

    @PostMapping("/members/subscribe/{planId}")
    Map<String, Object> subscribe(@RequestHeader("X-User-Email") String email,
                                  @RequestHeader("X-Internal-Key") String internalKey,
                                  @PathVariable("planId") Long planId);
}