package com.gympro.member_service.service;

import com.gympro.member_service.client.NotificationClient;

import com.gympro.member_service.dto.DashboardResponse;
import java.math.BigDecimal;
import com.gympro.member_service.entity.*;
import com.gympro.member_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationClient notificationClient;
    
    public Subscription getMyActiveSubscription(String email) {
        Member member = getByEmail(email);
        return getActiveSubscription(member.getId());
    }

    public Member createProfile(String email, Member req) {
        if (memberRepository.existsByUserEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Profile already exists");
        }
        req.setId(null);
        req.setUserEmail(email);
        return memberRepository.save(req);
    }

    public Member getByEmail(String email) {
        return memberRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Create your profile first"));
    }

    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
    }

    public List<Member> getAll() {
        return memberRepository.findAll();
    }

    public Subscription subscribe(String email, Long planId) {
        Member member = getByEmail(email);
        MembershipPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));

        if (subscriptionRepository.findFirstByMemberIdAndStatus(member.getId(), SubscriptionStatus.ACTIVE).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active subscription");
        }

        LocalDate today = LocalDate.now();
        Subscription sub = Subscription.builder()
                .memberId(member.getId())
                .plan(plan)
                .startDate(today)
                .endDate(today.plusDays(plan.getDurationDays()))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        return subscriptionRepository.save(sub);
    }

    public Subscription getActiveSubscription(Long memberId) {
        return subscriptionRepository.findFirstByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription"));
    }

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void expireSubscriptions() {
        List<Subscription> expired = subscriptionRepository
                .findByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, LocalDate.now());
        expired.forEach(s -> s.setStatus(SubscriptionStatus.EXPIRED));
        subscriptionRepository.saveAll(expired);
    }
    public DashboardResponse dashboard() {
        LocalDate today = LocalDate.now();

        List<DashboardResponse.ExpiringSoon> expiring = subscriptionRepository
                .findByStatusAndEndDateBetween(SubscriptionStatus.ACTIVE, today, today.plusDays(7))
                .stream()
                .map(s -> new DashboardResponse.ExpiringSoon(
                        s.getMemberId(),
                        memberRepository.findById(s.getMemberId()).map(Member::getName).orElse("Unknown"),
                        s.getPlan().getName(),
                        s.getEndDate().toString()))
                .toList();

        BigDecimal monthRevenue = subscriptionRepository.revenueSince(today.withDayOfMonth(1));

        return new DashboardResponse(
                memberRepository.count(),
                subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE),
                subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED),
                subscriptionRepository.totalRevenue(),
                monthRevenue,
                expiring);
    }
    
 // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendExpiryReminders() {
        LocalDate target = LocalDate.now().plusDays(2);
        List<Subscription> due = subscriptionRepository
                .findByStatusAndEndDateAndReminderSentFalse(SubscriptionStatus.ACTIVE, target);

        for (Subscription s : due) {
            memberRepository.findById(s.getMemberId()).ifPresent(member -> {
                try {
                    notificationClient.send(Map.of(
                            "to", member.getUserEmail(),
                            "subject", "GymPro: your membership ends soon",
                            "message", "Hi " + member.getName() + ", your " + s.getPlan().getName()
                                    + " membership ends on " + s.getEndDate()
                                    + ". Renew soon to keep your access."));
                    s.setReminderSent(true);
                    subscriptionRepository.save(s);
                } catch (Exception e) {
                    log.warn("Reminder failed for {}: {}", member.getUserEmail(), e.getMessage());
                }
            });
        }
    }
}