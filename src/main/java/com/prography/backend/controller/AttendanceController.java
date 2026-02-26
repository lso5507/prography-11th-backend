package com.prography.backend.controller;

import com.prography.backend.api.ApiResponse;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceDto.AttendanceResponse>> checkIn(@Valid @RequestBody AttendanceDto.CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(attendanceService.checkIn(request)));
    }
}
