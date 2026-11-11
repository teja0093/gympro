package com.gympro.attendance_service.controller;

import com.gympro.attendance_service.entity.Attendance;
import com.gympro.attendance_service.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public Attendance checkIn(@RequestHeader("X-User-Email") String email) {
        return attendanceService.checkIn(email);
    }

    @PostMapping("/check-out")
    public Attendance checkOut(@RequestHeader("X-User-Email") String email) {
        return attendanceService.checkOut(email);
    }

    @GetMapping("/me")
    public List<Attendance> myHistory(@RequestHeader("X-User-Email") String email) {
        return attendanceService.myHistory(email);
    }

    @GetMapping("/member/{id}")
    public List<Attendance> memberHistory(@RequestHeader("X-User-Role") String role,
                                          @PathVariable Long id) {
        if (!role.equals("ADMIN") && !role.equals("TRAINER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission");
        }
        return attendanceService.historyOf(id);
    }
}