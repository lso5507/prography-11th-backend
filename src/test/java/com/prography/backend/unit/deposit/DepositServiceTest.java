package com.prography.backend.unit.deposit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.Team;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.DepositHistoryRepository;
import com.prography.backend.service.DepositService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    @Mock
    private DepositHistoryRepository depositHistoryRepository;

    @InjectMocks
    private DepositService depositService;

    @Test
    @DisplayName("보증금보다 큰 패널티 차감 시 DEPOSIT_INSUFFICIENT")
    void applyPenalty_insufficient() {
        CohortMember cm = createCohortMember(1000);

        AppException ex = assertThrows(AppException.class, () -> depositService.applyPenalty(cm, 2000, null, "TEST"));

        assertEquals(ErrorCode.DEPOSIT_INSUFFICIENT, ex.getErrorCode());
    }

    private CohortMember createCohortMember(int deposit) {
        Cohort cohort = new Cohort("11기", 11, true);
        Member member = new Member("u1", "pw", "회원", "010-0000-0000", MemberRole.MEMBER, MemberStatus.ACTIVE);
        Part part = new Part(cohort, "SERVER");
        Team team = new Team(cohort, "Team A");
        return new CohortMember(cohort, member, part, team, deposit);
    }
}
