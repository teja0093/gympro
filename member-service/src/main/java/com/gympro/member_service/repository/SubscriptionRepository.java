package com.gympro.member_service.repository;

import com.gympro.member_service.entity.Subscription;
import com.gympro.member_service.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByMemberIdAndStatus(Long memberId, SubscriptionStatus status);
    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);
}