package com.gympro.workout_service.repository;

import com.gympro.workout_service.entity.DietPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DietPlanRepository extends JpaRepository<DietPlan, Long> {
    List<DietPlan> findByMemberEmail(String memberEmail);
}