package com.prography.backend.dto;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class AuthDto {

    public record LoginRequest(
        @NotBlank(message = "loginId는 필수입니다") String loginId,
        @NotBlank(message = "password는 필수입니다") String password
    ) {
    }

    public record LoginResponse(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }
}
