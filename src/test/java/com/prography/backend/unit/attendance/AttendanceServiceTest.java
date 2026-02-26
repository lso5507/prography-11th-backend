package com.prography.backend.unit.attendance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.prography.backend.domain.AttendanceSource;
import com.prography.backend.domain.AttendanceStatus;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.entity.Attendance;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.entity.Team;
import com.prography.backend.repository.AttendanceRepository;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.service.AttendanceService;
import com.prography.backend.service.CohortResolver;
import com.prography.backend.service.DepositService;
import com.prography.backend.service.MemberService;
import com.prography.backend.service.QrCodeService;
import com.prography.backend.service.SessionService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private CohortMemberRepository cohortMemberRepository;
    @Mock private MemberService memberService;
    @Mock private SessionService sessionService;
    @Mock private QrCodeService qrCodeService;
    @Mock private CohortResolver cohortResolver;
    @Mock private DepositService depositService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    @DisplayName("일정별 출결 요약 집계")
    void getSessionSummary_aggregates() {
        Cohort cohort = new Cohort("11기", 11, true);
        SessionEntity session = new SessionEntity(cohort, "세션", LocalDate.now(), LocalTime.NOON, LocalTime.MAX, "강남",
            com.prography.backend.domain.SessionStatus.IN_PROGRESS);
        when(sessionService.findSession(1L)).thenReturn(session);

        CohortMember cm = createCm();
        when(cohortMemberRepository.findByCohort(session.getCohort())).thenReturn(List.of(cm));

        Attendance a1 = new Attendance(cm, session, AttendanceStatus.PRESENT, null, 0, AttendanceSource.MANUAL,
            LocalDateTime.now(), null);
        when(attendanceRepository.findByCohortMemberAndSession(cm, session)).thenReturn(Optional.of(a1));

        AttendanceDto.MemberSessionSummaryResponse summary = attendanceService.getSessionSummary(1L).get(0);

        assertEquals(1, summary.present() + summary.absent() + summary.late() + summary.excused());
    }

    private CohortMember createCm() {
        Cohort cohort = new Cohort("11기", 11, true);
        Member member = new Member("u1", "pw", "회원", "010-0000-0000", com.prography.backend.domain.MemberRole.MEMBER,
            com.prography.backend.domain.MemberStatus.ACTIVE);
        Part part = new Part(cohort, "SERVER");
        Team team = new Team(cohort, "Team A");
        return new CohortMember(cohort, member, part, team, 100000);
    }
}
