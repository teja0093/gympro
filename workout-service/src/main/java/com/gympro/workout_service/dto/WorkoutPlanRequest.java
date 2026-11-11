package com.gympro.workout_service.dto;

import com.gympro.workout_service.entity.Exercise;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class WorkoutPlanRequest {
    @NotBlank private String memberEmail;
    @NotBlank private String title;
    private String goal;
    private List<Exercise> exercises;
}