package com.prography.backend.controller;

import com.prography.backend.api.ApiResponse;
import com.prography.backend.dto.CohortDto;
import com.prography.backend.dto.DepositDto;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.service.CohortService;
import com.prography.backend.service.DepositService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminCohortController {

    private final CohortService cohortService;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositService depositService;

    public AdminCohortController(CohortService cohortService,
                                 CohortMemberRepository cohortMemberRepository,
                                 DepositService depositService) {
        this.cohortService = cohortService;
        this.cohortMemberRepository = cohortMemberRepository;
        this.depositService = depositService;
    }

    @GetMapping("/cohorts")
    public ApiResponse<List<CohortDto.CohortListItem>> getCohorts() {
        return ApiResponse.success(cohortService.getCohorts());
    }

    @GetMapping("/cohorts/{id}")
    public ApiResponse<CohortDto.CohortDetailResponse> getCohortDetail(@PathVariable Long id) {
        return ApiResponse.success(cohortService.getCohortDetail(id));
    }

    @GetMapping("/cohort-members/{id}/deposits")
    public ApiResponse<List<DepositDto.DepositHistoryResponse>> getDepositHistory(@PathVariable Long id) {
        CohortMember cohortMember = cohortMemberRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
        return ApiResponse.success(
            depositService.getHistories(cohortMember).stream()
                .map(d -> new DepositDto.DepositHistoryResponse(
                    d.getId(),
                    d.getCohortMember().getId(),
                    d.getType(),
                    d.getAmount(),
                    d.getBalanceAfter(),
                    d.getAttendanceId(),
                    d.getDescription(),
                    d.getCreatedAt()
                ))
                .toList()
        );
    }
}
