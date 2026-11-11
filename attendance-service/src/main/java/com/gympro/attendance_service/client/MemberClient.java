package com.gympro.attendance_service.client;

import com.gympro.attendance_service.dto.MemberDto;
import com.gympro.attendance_service.dto.SubscriptionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "member-service")
public interface MemberClient {

    @GetMapping("/members/me")
    MemberDto getMe(@RequestHeader("X-User-Email") String email);

    @GetMapping("/members/{id}/subscription")
    SubscriptionDto getActiveSubscription(@PathVariable("id") Long memberId);
}