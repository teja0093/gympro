package com.gympro.workout_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "diet_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DietPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String memberEmail;
    private String trainerEmail;
    private String title;

    @Column(length = 2000)
    private String meals;

    private Integer caloriesPerDay;
    private LocalDate createdDate;
}