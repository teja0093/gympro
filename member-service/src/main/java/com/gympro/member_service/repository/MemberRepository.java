package com.gympro.member_service.repository;

import com.gympro.member_service.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUserEmail(String userEmail);
    boolean existsByUserEmail(String userEmail);
}