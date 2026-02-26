package com.prography.backend.dto;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class MemberDto {

    public record MemberSimpleResponse(
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

    public record MemberDetailResponse(
        Long id,
        String loginId,
        String name,
        String phone,
        MemberStatus status,
        MemberRole role,
        Integer generation,
        String partName,
        String teamName,
        Integer deposit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }

    public record MemberWithdrawResponse(Long id, String loginId, String name, MemberStatus status, LocalDateTime updatedAt) {
    }

    public record CreateMemberRequest(
        @NotBlank(message = "loginId는 필수입니다") String loginId,
        @NotBlank(message = "password는 필수입니다") String password,
        @NotBlank(message = "name은 필수입니다") String name,
        @NotBlank(message = "phone은 필수입니다") String phone,
        Long cohortId,
        Long partId,
        Long teamId
    ) {
    }

    public record UpdateMemberRequest(
        String name,
        String phone,
        Long cohortId,
        Long partId,
        Long teamId
    ) {
    }
}
