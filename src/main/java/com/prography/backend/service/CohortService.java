package com.prography.backend.service;

import com.prography.backend.dto.CohortDto;
import com.prography.backend.entity.Cohort;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.TeamRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CohortService {

    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;

    public CohortService(CohortRepository cohortRepository, PartRepository partRepository, TeamRepository teamRepository) {
        this.cohortRepository = cohortRepository;
        this.partRepository = partRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public List<CohortDto.CohortListItem> getCohorts() {
        return cohortRepository.findAll().stream()
            .map(c -> new CohortDto.CohortListItem(c.getId(), c.getGeneration(), c.getName(), c.getCreatedAt()))
            .toList();
    }

    @Transactional(readOnly = true)
    public CohortDto.CohortDetailResponse getCohortDetail(Long cohortId) {
        Cohort cohort = cohortRepository.findById(cohortId)
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_NOT_FOUND));

        List<CohortDto.PartItem> parts = partRepository.findByCohort(cohort).stream()
            .map(p -> new CohortDto.PartItem(p.getId(), p.getName()))
            .toList();
        List<CohortDto.TeamItem> teams = teamRepository.findByCohort(cohort).stream()
            .map(t -> new CohortDto.TeamItem(t.getId(), t.getName()))
            .toList();
        return new CohortDto.CohortDetailResponse(cohort.getId(), cohort.getGeneration(), cohort.getName(), parts, teams,
            cohort.getCreatedAt());
    }
}
