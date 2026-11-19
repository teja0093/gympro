package com.gympro.attendance_service.service;

import com.gympro.attendance_service.client.MemberClient;
import com.gympro.attendance_service.dto.MemberDto;
import com.gympro.attendance_service.dto.SubscriptionDto;
import com.gympro.attendance_service.entity.Attendance;
import com.gympro.attendance_service.repository.AttendanceRepository;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private MemberClient memberClient;

    @InjectMocks private AttendanceService attendanceService;

    private MemberDto member;

    @BeforeEach
    void setUp() {
        member = new MemberDto();
        member.setId(1L);
        member.setName("Naveen");
    }

    // Builds a fake 404 response from member-service
    private FeignException.NotFound notFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/members/1/subscription",
                Map.<String, Collection<String>>of(), null, StandardCharsets.UTF_8, null);
        return new FeignException.NotFound("not found", request, null, Map.of());
    }

    @Test
    void checkIn_activeSubscription_savesAttendance() {
        when(memberClient.getMe("naveen@gmail.com")).thenReturn(member);
        when(memberClient.getActiveSubscription(1L)).thenReturn(new SubscriptionDto());
        when(attendanceRepository.findFirstByMemberIdAndCheckOutIsNull(1L)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        Attendance result = attendanceService.checkIn("naveen@gmail.com");

        assertEquals(1L, result.getMemberId());
        assertNotNull(result.getCheckIn());
        assertNull(result.getCheckOut());
    }

    @Test
    void checkIn_expiredSubscription_isForbidden() {
        when(memberClient.getMe("naveen@gmail.com")).thenReturn(member);
        when(memberClient.getActiveSubscription(1L)).thenThrow(notFound());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> attendanceService.checkIn("naveen@gmail.com"));

        assertEquals(403, ex.getStatusCode().value());
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void checkIn_alreadyCheckedIn_isConflict() {
        when(memberClient.getMe("naveen@gmail.com")).thenReturn(member);
        when(memberClient.getActiveSubscription(1L)).thenReturn(new SubscriptionDto());
        when(attendanceRepository.findFirstByMemberIdAndCheckOutIsNull(1L))
                .thenReturn(Optional.of(new Attendance()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> attendanceService.checkIn("naveen@gmail.com"));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void checkIn_memberHasNoProfile_isNotFound() {
        when(memberClient.getMe("new@gmail.com")).thenThrow(notFound());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> attendanceService.checkIn("new@gmail.com"));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void checkOut_setsCheckOutTime() {
        Attendance open = Attendance.builder().id(5L).memberId(1L).build();
        when(memberClient.getMe("naveen@gmail.com")).thenReturn(member);
        when(attendanceRepository.findFirstByMemberIdAndCheckOutIsNull(1L)).thenReturn(Optional.of(open));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        Attendance result = attendanceService.checkOut("naveen@gmail.com");

        assertNotNull(result.getCheckOut());
    }

    @Test
    void checkOut_notCheckedIn_isBadRequest() {
        when(memberClient.getMe("naveen@gmail.com")).thenReturn(member);
        when(attendanceRepository.findFirstByMemberIdAndCheckOutIsNull(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> attendanceService.checkOut("naveen@gmail.com"));

        assertEquals(400, ex.getStatusCode().value());
    }
}