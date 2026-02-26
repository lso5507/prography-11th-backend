package com.prography.backend.controller;

import com.prography.backend.api.ApiResponse;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.dto.MemberDto;
import com.prography.backend.service.AttendanceService;
import com.prography.backend.service.MemberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberService memberService;
    private final AttendanceService attendanceService;

    public MemberController(MemberService memberService, AttendanceService attendanceService) {
        this.memberService = memberService;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/members/{id}")
    public ApiResponse<MemberDto.MemberSimpleResponse> getMember(@PathVariable Long id) {
        return ApiResponse.success(memberService.getMember(id));
    }

    @GetMapping("/members/{id}/attendance-summary")
    public ApiResponse<AttendanceDto.AttendanceSummaryResponse> getAttendanceSummary(@PathVariable Long id) {
        return ApiResponse.success(attendanceService.getMySummary(id));
    }

    @GetMapping("/attendances")
    public ApiResponse<java.util.List<AttendanceDto.MyAttendanceResponse>> getAttendances(@RequestParam Long memberId) {
        return ApiResponse.success(attendanceService.getMyAttendances(memberId));
    }
}
