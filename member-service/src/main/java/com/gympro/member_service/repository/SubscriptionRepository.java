package com.gympro.member_service.repository;

import com.gympro.member_service.entity.Subscription;
import com.gympro.member_service.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findFirstByMemberIdAndStatus(Long memberId, SubscriptionStatus status);

    List<Subscription> findByStatusAndEndDateBefore(SubscriptionStatus status, LocalDate date);

    long countByStatus(SubscriptionStatus status);

    List<Subscription> findByStatusAndEndDateBetween(SubscriptionStatus status, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(s.plan.price), 0) FROM Subscription s")
    BigDecimal totalRevenue();

    @Query("SELECT COALESCE(SUM(s.plan.price), 0) FROM Subscription s WHERE s.startDate >= :from")
    BigDecimal revenueSince(LocalDate from);
}