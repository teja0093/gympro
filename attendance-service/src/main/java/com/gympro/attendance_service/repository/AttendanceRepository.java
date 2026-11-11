package com.gympro.attendance_service.repository;

import com.gympro.attendance_service.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findFirstByMemberIdAndCheckOutIsNull(Long memberId);
    List<Attendance> findByMemberIdOrderByCheckInDesc(Long memberId);
}