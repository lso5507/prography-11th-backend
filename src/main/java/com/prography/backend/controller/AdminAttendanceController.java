package com.prography.backend.controller;

import com.prography.backend.api.ApiResponse;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.service.AttendanceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/attendances")
public class AdminAttendanceController {

    private final AttendanceService attendanceService;

    public AdminAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceDto.AttendanceResponse>> register(
        @Valid @RequestBody AttendanceDto.RegisterAttendanceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(attendanceService.register(request)));
    }

    @PutMapping("/{id}")
    public ApiResponse<AttendanceDto.AttendanceResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody AttendanceDto.UpdateAttendanceRequest request
    ) {
        return ApiResponse.success(attendanceService.update(id, request));
    }

    @GetMapping("/sessions/{sessionId}/summary")
    public ApiResponse<List<AttendanceDto.MemberSessionSummaryResponse>> sessionSummary(@PathVariable Long sessionId) {
        return ApiResponse.success(attendanceService.getSessionSummary(sessionId));
    }

    @GetMapping("/members/{memberId}")
    public ApiResponse<AttendanceDto.MemberAttendanceDetailResponse> memberAttendances(@PathVariable Long memberId) {
        return ApiResponse.success(attendanceService.getMemberAttendances(memberId));
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<AttendanceDto.SessionAttendancesResponse> sessionAttendances(@PathVariable Long sessionId) {
        return ApiResponse.success(attendanceService.getSessionAttendances(sessionId));
    }
}
