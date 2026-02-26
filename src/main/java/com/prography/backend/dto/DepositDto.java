package com.prography.backend.dto;

import com.prography.backend.domain.DepositType;
import java.time.LocalDateTime;

public class DepositDto {

    public record DepositHistoryResponse(
        Long id,
        Long cohortMemberId,
        DepositType type,
        int amount,
        int balanceAfter,
        Long attendanceId,
        String description,
        LocalDateTime createdAt
    ) {
    }
}
