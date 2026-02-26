package com.prography.backend.service;

import com.prography.backend.domain.SessionStatus;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.dto.SessionDto;
import com.prography.backend.entity.Attendance;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.AttendanceRepository;
import com.prography.backend.repository.SessionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CohortResolver cohortResolver;
    private final QrCodeService qrCodeService;
    private final AttendanceRepository attendanceRepository;

    public SessionService(SessionRepository sessionRepository, CohortResolver cohortResolver, QrCodeService qrCodeService,
                          AttendanceRepository attendanceRepository) {
        this.sessionRepository = sessionRepository;
        this.cohortResolver = cohortResolver;
        this.qrCodeService = qrCodeService;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    public List<SessionDto.SessionResponse> getMemberSessions() {
        Cohort current = cohortResolver.getCurrentCohort();
        return sessionRepository.findByCohortAndStatusNotOrderBySessionDateDescStartTimeDesc(current, SessionStatus.CANCELLED)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SessionDto.SessionResponse> getAdminSessions(LocalDate dateFrom, LocalDate dateTo, SessionStatus status) {
        Cohort current = cohortResolver.getCurrentCohort();
        return sessionRepository.findByCohortOrderBySessionDateDescStartTimeDesc(current)
            .stream()
            .filter(s -> dateFrom == null || !s.getSessionDate().isBefore(dateFrom))
            .filter(s -> dateTo == null || !s.getSessionDate().isAfter(dateTo))
            .filter(s -> status == null || s.getStatus() == status)
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public SessionDto.SessionResponse create(SessionDto.CreateSessionRequest request) {
        Cohort current = cohortResolver.getCurrentCohort();
        SessionEntity session = new SessionEntity(
            current,
            request.title(),
            request.date(),
            request.time(),
            request.time(),
            request.location(),
            SessionStatus.SCHEDULED
        );
        SessionEntity saved = sessionRepository.save(session);
        qrCodeService.createForSession(saved);
        return toDto(saved);
    }

    @Transactional
    public SessionDto.SessionResponse update(Long sessionId, SessionDto.UpdateSessionRequest request) {
        SessionEntity session = findSession(sessionId);
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new AppException(ErrorCode.SESSION_ALREADY_CANCELLED);
        }
        session.update(request.title(), request.date(), request.time(), request.location(), request.status());
        return toDto(session);
    }

    @Transactional
    public SessionDto.SessionResponse delete(Long sessionId) {
        SessionEntity session = findSession(sessionId);
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new AppException(ErrorCode.SESSION_ALREADY_CANCELLED);
        }
        session.cancel();
        qrCodeService.getActiveBySession(session);
        return toDto(session);
    }

    @Transactional(readOnly = true)
    public SessionEntity findSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
            .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
    }

    private SessionDto.SessionResponse toDto(SessionEntity session) {
        QrCode qrCode = qrCodeService.getActiveBySession(session);
        if (qrCode != null && qrCode.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            qrCode = null;
        }
        List<Attendance> attendances = attendanceRepository.findBySessionOrderByCreatedAtDesc(session);
        int present = (int) attendances.stream().filter(a -> a.getStatus() == com.prography.backend.domain.AttendanceStatus.PRESENT).count();
        int absent = (int) attendances.stream().filter(a -> a.getStatus() == com.prography.backend.domain.AttendanceStatus.ABSENT).count();
        int late = (int) attendances.stream().filter(a -> a.getStatus() == com.prography.backend.domain.AttendanceStatus.LATE).count();
        int excused = (int) attendances.stream().filter(a -> a.getStatus() == com.prography.backend.domain.AttendanceStatus.EXCUSED).count();
        SessionDto.AttendanceSummary summary = new SessionDto.AttendanceSummary(present, absent, late, excused,
            present + absent + late + excused);

        return new SessionDto.SessionResponse(
            session.getId(),
            session.getCohort().getId(),
            session.getTitle(),
            session.getSessionDate(),
            session.getStartTime(),
            session.getLocation(),
            session.getStatus(),
            summary,
            qrCode != null,
            session.getCreatedAt(),
            session.getUpdatedAt()
        );
    }
}
