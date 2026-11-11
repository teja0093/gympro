package com.gympro.attendance_service.service;

import com.gympro.attendance_service.client.MemberClient;
import com.gympro.attendance_service.dto.MemberDto;
import com.gympro.attendance_service.entity.Attendance;
import com.gympro.attendance_service.repository.AttendanceRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MemberClient memberClient;

    public Attendance checkIn(String email) {
        MemberDto member = findMember(email);

        // Ask member-service if the subscription is active
        try {
            memberClient.getActiveSubscription(member.getId());
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No active membership. Please renew your plan.");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Membership service is unavailable, try again later");
        }

        if (attendanceRepository.findFirstByMemberIdAndCheckOutIsNull(member.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already checked in");
        }

        return attendanceRepository.save(Attendance.builder()
                .memberId(member.getId())
                .checkIn(LocalDateTime.now())
                .build());
    }

    public Attendance checkOut(String email) {
        MemberDto member = findMember(email);
        Attendance open = attendanceRepository.findFirstByMemberIdAndCheckOutIsNull(member.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not checked in"));
        open.setCheckOut(LocalDateTime.now());
        return attendanceRepository.save(open);
    }

    public List<Attendance> myHistory(String email) {
        return attendanceRepository.findByMemberIdOrderByCheckInDesc(findMember(email).getId());
    }

    public List<Attendance> historyOf(Long memberId) {
        return attendanceRepository.findByMemberIdOrderByCheckInDesc(memberId);
    }

    private MemberDto findMember(String email) {
        try {
            return memberClient.getMe(email);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Create your member profile first");
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Membership service is unavailable, try again later");
        }
    }
}