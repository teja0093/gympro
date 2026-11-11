package com.gympro.member_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "membership_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembershipPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Positive
    private int durationDays;

    @NotNull @Positive
    private BigDecimal price;

    private String description;
}