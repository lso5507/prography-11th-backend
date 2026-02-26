package com.prography.backend.unit.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.dto.MemberDto;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.Team;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.MemberRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.TeamRepository;
import com.prography.backend.service.DepositService;
import com.prography.backend.service.MemberService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock private MemberRepository memberRepository;
    @Mock private CohortRepository cohortRepository;
    @Mock private PartRepository partRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private CohortMemberRepository cohortMemberRepository;
    @Mock private DepositService depositService;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("중복 loginId 회원 등록 시 DUPLICATE_LOGIN_ID")
    void createMember_duplicate_loginId() {
        when(memberRepository.existsByLoginId("dup-user")).thenReturn(true);

        MemberDto.CreateMemberRequest req = new MemberDto.CreateMemberRequest(
            "dup-user", "pass1234", "회원", "010-1111-1111", 1L, 1L, 1L
        );

        AppException ex = assertThrows(AppException.class, () -> memberService.createMember(req));
        assertEquals(ErrorCode.DUPLICATE_LOGIN_ID, ex.getErrorCode());
    }

    @Test
    @DisplayName("존재하지 않는 팀 지정 시 TEAM_NOT_FOUND")
    void createMember_team_not_found() {
        when(memberRepository.existsByLoginId("new-user")).thenReturn(false);
        Cohort cohort = new Cohort("11기", 11, true);
        Part part = new Part(cohort, "SERVER");

        when(cohortRepository.findById(1L)).thenReturn(Optional.of(cohort));
        when(partRepository.findByIdAndCohort(1L, cohort)).thenReturn(Optional.of(part));
        when(teamRepository.findByIdAndCohort(999L, cohort)).thenReturn(Optional.empty());

        MemberDto.CreateMemberRequest req = new MemberDto.CreateMemberRequest(
            "new-user", "pass1234", "회원", "010-2222-2222", 1L, 1L, 999L
        );

        AppException ex = assertThrows(AppException.class, () -> memberService.createMember(req));
        assertEquals(ErrorCode.TEAM_NOT_FOUND, ex.getErrorCode());
    }
}
