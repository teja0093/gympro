package com.gympro.member_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private MembershipPlan plan;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
}