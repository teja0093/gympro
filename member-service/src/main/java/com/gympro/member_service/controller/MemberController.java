package com.gympro.member_service.controller;

import com.gympro.member_service.dto.DashboardResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.gympro.member_service.entity.Member;
import com.gympro.member_service.entity.Subscription;
import com.gympro.member_service.service.MemberService;
import com.gympro.member_service.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    
    @Value("${internal.key}")
    private String internalKey;
    
    @GetMapping("/me/subscription")
    public Subscription mySubscription(@RequestHeader("X-User-Email") String email) {
        return memberService.getMyActiveSubscription(email);
    }

    @PostMapping("/profile")
    public Member createProfile(@RequestHeader("X-User-Email") String email,
                                @RequestBody Member member) {
        return memberService.createProfile(email, member);
    }

    @GetMapping("/me")
    public Member me(@RequestHeader("X-User-Email") String email) {
        return memberService.getByEmail(email);
    }

    @GetMapping
    public List<Member> all(@RequestHeader("X-User-Role") String role) {
        RoleGuard.requireRole(role, "ADMIN", "TRAINER");
        return memberService.getAll();
    }

    @PostMapping("/subscribe/{planId}")
    public Subscription subscribe(@RequestHeader("X-User-Email") String email,
                                  @RequestHeader(value = "X-Internal-Key", required = false) String key,
                                  @PathVariable Long planId) {
        if (key == null || !key.equals(internalKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please pay for the plan first");
        }
        return memberService.subscribe(email, planId);
    }

    @GetMapping("/{id}/subscription")
    public Subscription activeSubscription(@PathVariable Long id) {
        return memberService.getActiveSubscription(id);
    }
    
    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@RequestHeader("X-User-Role") String role) {
        RoleGuard.requireRole(role, "ADMIN");
        return memberService.dashboard();
    }
    
    @PostMapping("/send-reminders")
    public String sendReminders(@RequestHeader("X-User-Role") String role) {
        RoleGuard.requireRole(role, "ADMIN");
        memberService.sendExpiryReminders();
        return "Reminder check done";
    }
}