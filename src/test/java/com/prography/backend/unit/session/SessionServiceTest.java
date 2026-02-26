package com.prography.backend.unit.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.prography.backend.domain.SessionStatus;
import com.prography.backend.dto.SessionDto;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.AttendanceRepository;
import com.prography.backend.repository.SessionRepository;
import com.prography.backend.service.CohortResolver;
import com.prography.backend.service.QrCodeService;
import com.prography.backend.service.SessionService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private CohortResolver cohortResolver;
    @Mock private QrCodeService qrCodeService;
    @Mock private AttendanceRepository attendanceRepository;

    @InjectMocks
    private SessionService sessionService;

    @Test
    @DisplayName("CANCELLED 일정 수정 시 SESSION_ALREADY_CANCELLED")
    void update_cancelled_session() {
        Cohort cohort = new Cohort("11기", 11, true);
        SessionEntity cancelled = new SessionEntity(cohort, "세션", LocalDate.now(), LocalTime.NOON, LocalTime.MIDNIGHT, "강남",
            SessionStatus.CANCELLED);
        when(sessionRepository.findById(1L)).thenReturn(Optional.of(cancelled));

        SessionDto.UpdateSessionRequest req = new SessionDto.UpdateSessionRequest(
            "세션수정", LocalDate.now(), LocalTime.NOON, "강남", SessionStatus.IN_PROGRESS
        );

        AppException ex = assertThrows(AppException.class, () -> sessionService.update(1L, req));
        assertEquals(ErrorCode.SESSION_ALREADY_CANCELLED, ex.getErrorCode());
    }
}
