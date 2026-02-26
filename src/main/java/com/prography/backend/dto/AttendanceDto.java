package com.prography.backend.dto;

import com.prography.backend.domain.AttendanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceDto {

    public record CheckInRequest(
        @NotBlank(message = "hashValue는 필수입니다") String hashValue,
        @NotNull(message = "memberId는 필수입니다") Long memberId
    ) {
    }

    public record RegisterAttendanceRequest(
        @NotNull(message = "sessionId는 필수입니다") Long sessionId,
        @NotNull(message = "memberId는 필수입니다") Long memberId,
        @NotNull(message = "status는 필수입니다") AttendanceStatus status,
        Integer lateMinutes,
        String reason
    ) {
    }

    public record UpdateAttendanceRequest(
        @NotNull(message = "status는 필수입니다") AttendanceStatus status,
        Integer lateMinutes,
        String reason
    ) {
    }

    public record AttendanceResponse(
        Long id,
        Long sessionId,
        Long memberId,
        AttendanceStatus status,
        Integer lateMinutes,
        int penaltyAmount,
        String reason,
        LocalDateTime checkedInAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }

    public record MyAttendanceResponse(
        Long id,
        Long sessionId,
        String sessionTitle,
        AttendanceStatus status,
        Integer lateMinutes,
        int penaltyAmount,
        String reason,
        LocalDateTime checkedInAt,
        LocalDateTime createdAt
    ) {
    }

    public record AttendanceSummaryResponse(
        Long memberId,
        long present,
        long absent,
        long late,
        long excused,
        int totalPenalty,
        Integer deposit
    ) {
    }

    public record MemberSessionSummaryResponse(
        Long memberId,
        String memberName,
        long present,
        long absent,
        long late,
        long excused,
        int totalPenalty,
        Integer deposit
    ) {
    }

    public record MemberAttendanceDetailResponse(
        Long memberId,
        String memberName,
        Integer generation,
        String partName,
        String teamName,
        Integer deposit,
        Integer excuseCount,
        List<AttendanceResponse> attendances
    ) {
    }

    public record SessionAttendancesResponse(Long sessionId, String sessionTitle, List<AttendanceResponse> attendances) {
    }
}
