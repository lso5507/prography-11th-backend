package com.prography.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CohortDto {

    public record CohortListItem(Long id, int generation, String name, LocalDateTime createdAt) {
    }

    public record PartItem(Long id, String name) {
    }

    public record TeamItem(Long id, String name) {
    }

    public record CohortDetailResponse(Long id, int generation, String name, List<PartItem> parts, List<TeamItem> teams,
                                       LocalDateTime createdAt) {
    }
}
