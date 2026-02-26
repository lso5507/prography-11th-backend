package com.prography.backend.service;

import com.prography.backend.domain.AttendanceSource;
import com.prography.backend.domain.AttendanceStatus;
import com.prography.backend.domain.MemberStatus;
import com.prography.backend.domain.SessionStatus;
import com.prography.backend.dto.AttendanceDto;
import com.prography.backend.entity.Attendance;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.AttendanceRepository;
import com.prography.backend.repository.CohortMemberRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final MemberService memberService;
    private final SessionService sessionService;
    private final QrCodeService qrCodeService;
    private final CohortResolver cohortResolver;
    private final DepositService depositService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             CohortMemberRepository cohortMemberRepository,
                             MemberService memberService,
                             SessionService sessionService,
                             QrCodeService qrCodeService,
                             CohortResolver cohortResolver,
                             DepositService depositService) {
        this.attendanceRepository = attendanceRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.memberService = memberService;
        this.sessionService = sessionService;
        this.qrCodeService = qrCodeService;
        this.cohortResolver = cohortResolver;
        this.depositService = depositService;
    }

    @Transactional
    public AttendanceDto.AttendanceResponse checkIn(AttendanceDto.CheckInRequest request) {
        QrCode qrCode = qrCodeService.findByHash(request.hashValue());
        if (qrCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.QR_EXPIRED);
        }
        SessionEntity session = qrCode.getSession();
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.SESSION_NOT_IN_PROGRESS);
        }

        Member member = memberService.findMember(request.memberId());
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.MEMBER_WITHDRAWN);
        }

        CohortMember cohortMember = cohortMemberRepository.findByMemberAndCohort(member, session.getCohort())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        attendanceRepository.findByCohortMemberAndSession(cohortMember, session)
            .ifPresent(it -> {
                throw new AppException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
            });

        LocalDateTime sessionAt = LocalDateTime.of(session.getSessionDate(), session.getStartTime());
        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status = now.isAfter(sessionAt) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;
        Integer lateMinutes = status == AttendanceStatus.LATE ? (int) Duration.between(sessionAt, now).toMinutes() : null;
        int penalty = calculatePenalty(status, lateMinutes);
        if (penalty > 0) {
            depositService.applyPenalty(cohortMember, penalty, null, "QR 체크인 패널티");
        }

        Attendance attendance = attendanceRepository.save(
            new Attendance(cohortMember, session, status, lateMinutes, penalty, AttendanceSource.QR, now, null)
        );
        return toAttendanceResponse(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.MyAttendanceResponse> getMyAttendances(Long memberId) {
        Member member = memberService.findMember(memberId);
        CohortMember cohortMember = cohortMemberRepository.findByMemberAndCohort(member, cohortResolver.getCurrentCohort())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));
        return attendanceRepository.findByCohortMemberOrderByCreatedAtDesc(cohortMember).stream()
            .map(this::toMyAttendanceResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceDto.AttendanceSummaryResponse getMySummary(Long memberId) {
        Member member = memberService.findMember(memberId);
        List<Attendance> attendances = attendanceRepository.findByCohortMember_Member_IdOrderByCreatedAtDesc(memberId);
        CohortMember cm = cohortMemberRepository.findByMemberAndCohort(member, cohortResolver.getCurrentCohort()).orElse(null);

        long present = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long late = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        long excused = attendances.stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count();
        int totalPenalty = attendances.stream().mapToInt(Attendance::getPenaltyAmount).sum();

        return new AttendanceDto.AttendanceSummaryResponse(memberId, present, absent, late, excused, totalPenalty,
            cm == null ? null : cm.getDepositBalance());
    }

    @Transactional
    public AttendanceDto.AttendanceResponse register(AttendanceDto.RegisterAttendanceRequest request) {
        SessionEntity session = sessionService.findSession(request.sessionId());
        Member member = memberService.findMember(request.memberId());
        CohortMember cohortMember = cohortMemberRepository.findByMemberAndCohort(member, session.getCohort())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        attendanceRepository.findByCohortMemberAndSession(cohortMember, session)
            .ifPresent(it -> {
                throw new AppException(ErrorCode.ATTENDANCE_ALREADY_CHECKED);
            });

        validateExcusedLimitForCreate(cohortMember, request.status());
        int penalty = calculatePenalty(request.status(), request.lateMinutes());
        if (penalty > 0) {
            depositService.applyPenalty(cohortMember, penalty, null,
                "출결 등록 - " + request.status() + " 패널티 " + penalty + "원");
        }
        if (request.status() == AttendanceStatus.EXCUSED) {
            cohortMember.increaseExcusedCount();
        }

        Attendance attendance = attendanceRepository.save(
            new Attendance(cohortMember, session, request.status(), request.lateMinutes(), penalty, AttendanceSource.MANUAL, null,
                request.reason())
        );
        return toAttendanceResponse(attendance);
    }

    @Transactional
    public AttendanceDto.AttendanceResponse update(Long attendanceId, AttendanceDto.UpdateAttendanceRequest request) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_NOT_FOUND));
        CohortMember cohortMember = attendance.getCohortMember();

        AttendanceStatus oldStatus = attendance.getStatus();
        int oldPenalty = attendance.getPenaltyAmount();
        int newPenalty = calculatePenalty(request.status(), request.lateMinutes());

        if (oldStatus != AttendanceStatus.EXCUSED && request.status() == AttendanceStatus.EXCUSED) {
            if (cohortMember.getExcusedCount() >= 3) {
                throw new AppException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
            }
            cohortMember.increaseExcusedCount();
        } else if (oldStatus == AttendanceStatus.EXCUSED && request.status() != AttendanceStatus.EXCUSED) {
            cohortMember.decreaseExcusedCount();
        }

        int diff = newPenalty - oldPenalty;
        if (diff > 0) {
            depositService.applyPenalty(cohortMember, diff, attendance.getId(), "출결 수정 - 추가 차감 " + diff + "원");
        } else if (diff < 0) {
            depositService.refund(cohortMember, -diff, attendance.getId(), "출결 수정 - 환급 " + (-diff) + "원");
        }

        attendance.update(request.status(), request.lateMinutes(), newPenalty, request.reason());
        return toAttendanceResponse(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceDto.MemberSessionSummaryResponse> getSessionSummary(Long sessionId) {
        SessionEntity session = sessionService.findSession(sessionId);
        List<CohortMember> members = cohortMemberRepository.findByCohort(session.getCohort());

        return members.stream().map(cm -> {
            Attendance attendance = attendanceRepository.findByCohortMemberAndSession(cm, session).orElse(null);
            long present = attendance != null && attendance.getStatus() == AttendanceStatus.PRESENT ? 1 : 0;
            long absent = attendance != null && attendance.getStatus() == AttendanceStatus.ABSENT ? 1 : 0;
            long late = attendance != null && attendance.getStatus() == AttendanceStatus.LATE ? 1 : 0;
            long excused = attendance != null && attendance.getStatus() == AttendanceStatus.EXCUSED ? 1 : 0;
            int totalPenalty = attendance == null ? 0 : attendance.getPenaltyAmount();
            return new AttendanceDto.MemberSessionSummaryResponse(
                cm.getMember().getId(),
                cm.getMember().getName(),
                present,
                absent,
                late,
                excused,
                totalPenalty,
                cm.getDepositBalance()
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public AttendanceDto.MemberAttendanceDetailResponse getMemberAttendances(Long memberId) {
        Member member = memberService.findMember(memberId);
        CohortMember cm = cohortMemberRepository.findByMemberAndCohort(member, cohortResolver.getCurrentCohort()).orElse(null);
        List<AttendanceDto.AttendanceResponse> list = attendanceRepository.findByCohortMember_Member_IdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(this::toAttendanceResponse)
            .toList();

        return new AttendanceDto.MemberAttendanceDetailResponse(
            member.getId(),
            member.getName(),
            cm == null ? null : cm.getCohort().getGeneration(),
            cm == null || cm.getPart() == null ? null : cm.getPart().getName(),
            cm == null || cm.getTeam() == null ? null : cm.getTeam().getName(),
            cm == null ? null : cm.getDepositBalance(),
            cm == null ? null : cm.getExcusedCount(),
            list
        );
    }

    @Transactional(readOnly = true)
    public AttendanceDto.SessionAttendancesResponse getSessionAttendances(Long sessionId) {
        SessionEntity session = sessionService.findSession(sessionId);
        List<AttendanceDto.AttendanceResponse> list = attendanceRepository.findBySessionOrderByCreatedAtDesc(session)
            .stream()
            .map(this::toAttendanceResponse)
            .toList();
        return new AttendanceDto.SessionAttendancesResponse(session.getId(), session.getTitle(), list);
    }

    private void validateExcusedLimitForCreate(CohortMember cohortMember, AttendanceStatus status) {
        if (status == AttendanceStatus.EXCUSED && cohortMember.getExcusedCount() >= 3) {
            throw new AppException(ErrorCode.EXCUSE_LIMIT_EXCEEDED);
        }
    }

    private int calculatePenalty(AttendanceStatus status, Integer lateMinutes) {
        return switch (status) {
            case PRESENT, EXCUSED -> 0;
            case ABSENT -> 10_000;
            case LATE -> Math.min(Math.max(lateMinutes == null ? 0 : lateMinutes, 0) * 500, 10_000);
        };
    }

    private AttendanceDto.AttendanceResponse toAttendanceResponse(Attendance attendance) {
        return new AttendanceDto.AttendanceResponse(
            attendance.getId(),
            attendance.getSession().getId(),
            attendance.getCohortMember().getMember().getId(),
            attendance.getStatus(),
            attendance.getStatus() == AttendanceStatus.LATE ? attendance.getLateMinutes() : null,
            attendance.getPenaltyAmount(),
            attendance.getReason(),
            attendance.getCheckedAt(),
            attendance.getCreatedAt(),
            attendance.getUpdatedAt()
        );
    }

    private AttendanceDto.MyAttendanceResponse toMyAttendanceResponse(Attendance attendance) {
        return new AttendanceDto.MyAttendanceResponse(
            attendance.getId(),
            attendance.getSession().getId(),
            attendance.getSession().getTitle(),
            attendance.getStatus(),
            attendance.getStatus() == AttendanceStatus.LATE ? attendance.getLateMinutes() : null,
            attendance.getPenaltyAmount(),
            attendance.getReason(),
            attendance.getCheckedAt(),
            attendance.getCreatedAt()
        );
    }
}
