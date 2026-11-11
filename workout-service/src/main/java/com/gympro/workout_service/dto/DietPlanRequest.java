package com.gympro.workout_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DietPlanRequest {
    @NotBlank private String memberEmail;
    @NotBlank private String title;
    private String meals;
    private Integer caloriesPerDay;
}