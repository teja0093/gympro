package com.gympro.workout_service.service;

import com.gympro.workout_service.dto.DietPlanRequest;
import com.gympro.workout_service.dto.WorkoutPlanRequest;
import com.gympro.workout_service.entity.DietPlan;
import com.gympro.workout_service.entity.Exercise;
import com.gympro.workout_service.entity.WorkoutPlan;
import com.gympro.workout_service.repository.DietPlanRepository;
import com.gympro.workout_service.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutPlanRepository workoutRepo;
    private final DietPlanRepository dietRepo;

    public WorkoutPlan createWorkout(String trainerEmail, WorkoutPlanRequest req) {
        List<Exercise> exercises = new ArrayList<>();
        if (req.getExercises() != null) {
            for (Exercise e : req.getExercises()) {
                e.setId(null);
                exercises.add(e);
            }
        }
        WorkoutPlan plan = WorkoutPlan.builder()
                .memberEmail(req.getMemberEmail())
                .trainerEmail(trainerEmail)
                .title(req.getTitle())
                .goal(req.getGoal())
                .createdDate(LocalDate.now())
                .exercises(exercises)
                .build();
        return workoutRepo.save(plan);
    }

    public DietPlan createDiet(String trainerEmail, DietPlanRequest req) {
        DietPlan plan = DietPlan.builder()
                .memberEmail(req.getMemberEmail())
                .trainerEmail(trainerEmail)
                .title(req.getTitle())
                .meals(req.getMeals())
                .caloriesPerDay(req.getCaloriesPerDay())
                .createdDate(LocalDate.now())
                .build();
        return dietRepo.save(plan);
    }

    public List<WorkoutPlan> myWorkouts(String email) {
        return workoutRepo.findByMemberEmail(email);
    }

    public List<DietPlan> myDiets(String email) {
        return dietRepo.findByMemberEmail(email);
    }

    public List<WorkoutPlan> workoutsOf(String memberEmail) {
        return workoutRepo.findByMemberEmail(memberEmail);
    }

    public void deleteWorkout(Long id) {
        if (!workoutRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout plan not found");
        }
        workoutRepo.deleteById(id);
    }
}