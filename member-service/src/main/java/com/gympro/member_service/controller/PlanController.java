package com.gympro.member_service.controller;

import com.gympro.member_service.entity.MembershipPlan;
import com.gympro.member_service.repository.MembershipPlanRepository;
import com.gympro.member_service.util.RoleGuard;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/plans")
@RequiredArgsConstructor
public class PlanController {

    private final MembershipPlanRepository planRepository;

    @PostMapping
    public MembershipPlan create(@RequestHeader("X-User-Role") String role,
                                 @Valid @RequestBody MembershipPlan plan) {
        RoleGuard.requireRole(role, "ADMIN");
        plan.setId(null);
        return planRepository.save(plan);
    }

    @GetMapping
    public List<MembershipPlan> getAll() {
        return planRepository.findAll();
    }

    @GetMapping("/{id}")
    public MembershipPlan getOne(@PathVariable Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
    }

    @PutMapping("/{id}")
    public MembershipPlan update(@RequestHeader("X-User-Role") String role,
                                 @PathVariable Long id,
                                 @Valid @RequestBody MembershipPlan plan) {
        RoleGuard.requireRole(role, "ADMIN");
        getOne(id);
        plan.setId(id);
        return planRepository.save(plan);
    }

    @DeleteMapping("/{id}")
    public String delete(@RequestHeader("X-User-Role") String role, @PathVariable Long id) {
        RoleGuard.requireRole(role, "ADMIN");
        getOne(id);
        planRepository.deleteById(id);
        return "Plan deleted";
    }
}