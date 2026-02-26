package com.prography.backend.dto;

import com.prography.backend.domain.SessionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SessionDto {

    public record AttendanceSummary(int present, int absent, int late, int excused, int total) {
    }

    public record SessionResponse(
        Long id,
        Long cohortId,
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status,
        AttendanceSummary attendanceSummary,
        boolean qrActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }

    public record CreateSessionRequest(
        @NotBlank(message = "title은 필수입니다") String title,
        @NotNull(message = "date는 필수입니다") LocalDate date,
        @NotNull(message = "time은 필수입니다") LocalTime time,
        @NotBlank(message = "location은 필수입니다") String location
    ) {
    }

    public record UpdateSessionRequest(
        String title,
        LocalDate date,
        LocalTime time,
        String location,
        SessionStatus status
    ) {
    }

    public record QrCodeResponse(Long id, Long sessionId, String hashValue, LocalDateTime createdAt, LocalDateTime expiresAt) {
    }
}
