package com.gympro.workout_service.controller;

import com.gympro.workout_service.dto.DietPlanRequest;
import com.gympro.workout_service.dto.WorkoutPlanRequest;
import com.gympro.workout_service.entity.DietPlan;
import com.gympro.workout_service.entity.WorkoutPlan;
import com.gympro.workout_service.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    private void requireTrainer(String role) {
        if (!role.equals("TRAINER") && !role.equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only trainers can do this");
        }
    }

    // ---- Trainer APIs ----

    @PostMapping("/plans")
    public WorkoutPlan createWorkout(@RequestHeader("X-User-Email") String email,
                                     @RequestHeader("X-User-Role") String role,
                                     @Valid @RequestBody WorkoutPlanRequest req) {
        requireTrainer(role);
        return workoutService.createWorkout(email, req);
    }

    @PostMapping("/diets")
    public DietPlan createDiet(@RequestHeader("X-User-Email") String email,
                               @RequestHeader("X-User-Role") String role,
                               @Valid @RequestBody DietPlanRequest req) {
        requireTrainer(role);
        return workoutService.createDiet(email, req);
    }

    @GetMapping("/member")
    public List<WorkoutPlan> workoutsOfMember(@RequestHeader("X-User-Role") String role,
                                              @RequestParam String email) {
        requireTrainer(role);
        return workoutService.workoutsOf(email);
    }

    @DeleteMapping("/plans/{id}")
    public String deleteWorkout(@RequestHeader("X-User-Role") String role,
                                @PathVariable Long id) {
        requireTrainer(role);
        workoutService.deleteWorkout(id);
        return "Workout plan deleted";
    }

    // ---- Member APIs ----

    @GetMapping("/my-plans")
    public List<WorkoutPlan> myWorkouts(@RequestHeader("X-User-Email") String email) {
        return workoutService.myWorkouts(email);
    }

    @GetMapping("/my-diets")
    public List<DietPlan> myDiets(@RequestHeader("X-User-Email") String email) {
        return workoutService.myDiets(email);
    }
}