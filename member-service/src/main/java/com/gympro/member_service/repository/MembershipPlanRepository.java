package com.gympro.member_service.repository;

import com.gympro.member_service.entity.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
}