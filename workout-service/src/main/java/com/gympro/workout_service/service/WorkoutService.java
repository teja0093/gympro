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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutPlanRepository workoutRepo;
    private final DietPlanRepository dietRepo;

    private ResponseStatusException conflict(String what) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "This " + what + " plan already exists for this member");
    }

    private ResponseStatusException notFound(String what) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " plan not found");
    }

    private List<Exercise> cleanExercises(List<Exercise> input) {
        List<Exercise> list = new ArrayList<>();
        if (input != null) {
            for (Exercise e : input) {
                e.setId(null);
                list.add(e);
            }
        }
        return list;
    }

    // ---------- Workout plans ----------

    public WorkoutPlan createWorkout(String trainerEmail, WorkoutPlanRequest req) {
        String member = req.getMemberEmail().trim();
        String title = req.getTitle().trim();
        if (!workoutRepo.findAllByMemberEmailIgnoreCaseAndTitleIgnoreCase(member, title).isEmpty()) {
            throw conflict("workout");
        }
        WorkoutPlan plan = WorkoutPlan.builder()
                .memberEmail(member)
                .trainerEmail(trainerEmail)
                .title(title)
                .goal(req.getGoal())
                .createdDate(LocalDate.now())
                .exercises(cleanExercises(req.getExercises()))
                .build();
        return workoutRepo.save(plan);
    }

    @Transactional
    public WorkoutPlan updateWorkout(Long id, WorkoutPlanRequest req) {
        WorkoutPlan plan = workoutRepo.findById(id).orElseThrow(() -> notFound("Workout"));
        String title = req.getTitle().trim();

        boolean clash = workoutRepo
                .findAllByMemberEmailIgnoreCaseAndTitleIgnoreCase(plan.getMemberEmail(), title)
                .stream().anyMatch(other -> !other.getId().equals(id));
        if (clash) throw conflict("workout");

        plan.setTitle(title);
        plan.setGoal(req.getGoal());
        plan.getExercises().clear();                       // keep the same list object (orphanRemoval)
        plan.getExercises().addAll(cleanExercises(req.getExercises()));
        return workoutRepo.save(plan);
    }

    public void deleteWorkout(Long id) {
        if (!workoutRepo.existsById(id)) throw notFound("Workout");
        workoutRepo.deleteById(id);
    }

    public List<WorkoutPlan> myWorkouts(String email) {
        return workoutRepo.findByMemberEmail(email);
    }

    public List<WorkoutPlan> workoutsOf(String memberEmail) {
        return workoutRepo.findByMemberEmail(memberEmail);
    }

    // ---------- Diet plans ----------

    public DietPlan createDiet(String trainerEmail, DietPlanRequest req) {
        String member = req.getMemberEmail().trim();
        String title = req.getTitle().trim();
        if (!dietRepo.findAllByMemberEmailIgnoreCaseAndTitleIgnoreCase(member, title).isEmpty()) {
            throw conflict("diet");
        }
        DietPlan plan = DietPlan.builder()
                .memberEmail(member)
                .trainerEmail(trainerEmail)
                .title(title)
                .meals(req.getMeals())
                .caloriesPerDay(req.getCaloriesPerDay())
                .createdDate(LocalDate.now())
                .build();
        return dietRepo.save(plan);
    }

    @Transactional
    public DietPlan updateDiet(Long id, DietPlanRequest req) {
        DietPlan plan = dietRepo.findById(id).orElseThrow(() -> notFound("Diet"));
        String title = req.getTitle().trim();

        boolean clash = dietRepo
                .findAllByMemberEmailIgnoreCaseAndTitleIgnoreCase(plan.getMemberEmail(), title)
                .stream().anyMatch(other -> !other.getId().equals(id));
        if (clash) throw conflict("diet");

        plan.setTitle(title);
        plan.setMeals(req.getMeals());
        plan.setCaloriesPerDay(req.getCaloriesPerDay());
        return dietRepo.save(plan);
    }

    public void deleteDiet(Long id) {
        if (!dietRepo.existsById(id)) throw notFound("Diet");
        dietRepo.deleteById(id);
    }

    public List<DietPlan> myDiets(String email) {
        return dietRepo.findByMemberEmail(email);
    }

    public List<DietPlan> dietsOf(String memberEmail) {
        return dietRepo.findByMemberEmail(memberEmail);
    }
}