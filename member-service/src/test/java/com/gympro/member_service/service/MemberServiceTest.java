package com.gympro.member_service.service;

import com.gympro.member_service.client.NotificationClient;
import com.gympro.member_service.entity.Member;
import com.gympro.member_service.entity.MembershipPlan;
import com.gympro.member_service.entity.Subscription;
import com.gympro.member_service.entity.SubscriptionStatus;
import com.gympro.member_service.repository.MemberRepository;
import com.gympro.member_service.repository.MembershipPlanRepository;
import com.gympro.member_service.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private MembershipPlanRepository planRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private MemberService memberService;

    private Member member() {
        return Member.builder().id(1L).userEmail("naveen@gmail.com").name("Naveen").build();
    }

    private MembershipPlan plan() {
        return MembershipPlan.builder().id(2L).name("Monthly")
                .durationDays(30).price(new BigDecimal("999")).build();
    }

    @Test
    void createProfile_newEmail_savesWithEmailFromToken() {
        when(memberRepository.existsByUserEmail("naveen@gmail.com")).thenReturn(false);
        when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

        Member req = Member.builder().id(99L).name("Naveen").phone("9999999999").build();
        Member saved = memberService.createProfile("naveen@gmail.com", req);

        assertEquals("naveen@gmail.com", saved.getUserEmail());
        assertNull(saved.getId());   // client-supplied id must be ignored
    }

    @Test
    void createProfile_duplicate_isConflict() {
        when(memberRepository.existsByUserEmail("naveen@gmail.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.createProfile("naveen@gmail.com", new Member()));

        assertEquals(409, ex.getStatusCode().value());
        verify(memberRepository, never()).save(any());
    }

    @Test
    void getByEmail_missing_isNotFound() {
        when(memberRepository.findByUserEmail("x@gmail.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.getByEmail("x@gmail.com"));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void subscribe_valid_createsActiveSubscriptionWithCorrectEndDate() {
        when(memberRepository.findByUserEmail("naveen@gmail.com")).thenReturn(Optional.of(member()));
        when(planRepository.findById(2L)).thenReturn(Optional.of(plan()));
        when(subscriptionRepository.findFirstByMemberIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        Subscription sub = memberService.subscribe("naveen@gmail.com", 2L);

        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        assertEquals(1L, sub.getMemberId());
        assertEquals(LocalDate.now(), sub.getStartDate());
        assertEquals(LocalDate.now().plusDays(30), sub.getEndDate());
    }

    @Test
    void subscribe_alreadyActive_isConflict() {
        when(memberRepository.findByUserEmail("naveen@gmail.com")).thenReturn(Optional.of(member()));
        when(planRepository.findById(2L)).thenReturn(Optional.of(plan()));
        when(subscriptionRepository.findFirstByMemberIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(new Subscription()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.subscribe("naveen@gmail.com", 2L));

        assertEquals(409, ex.getStatusCode().value());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_planNotFound_isNotFound() {
        when(memberRepository.findByUserEmail("naveen@gmail.com")).thenReturn(Optional.of(member()));
        when(planRepository.findById(5L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.subscribe("naveen@gmail.com", 5L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void subscribe_noProfile_isNotFound() {
        when(memberRepository.findByUserEmail("new@gmail.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.subscribe("new@gmail.com", 2L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void getActiveSubscription_none_isNotFound() {
        when(subscriptionRepository.findFirstByMemberIdAndStatus(1L, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> memberService.getActiveSubscription(1L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void expireSubscriptions_marksOldActiveOnesExpired() {
        Subscription old = Subscription.builder().id(1L).status(SubscriptionStatus.ACTIVE).build();
        when(subscriptionRepository.findByStatusAndEndDateBefore(eq(SubscriptionStatus.ACTIVE), any(LocalDate.class)))
                .thenReturn(List.of(old));

        memberService.expireSubscriptions();

        assertEquals(SubscriptionStatus.EXPIRED, old.getStatus());
        verify(subscriptionRepository).saveAll(List.of(old));
    }
}