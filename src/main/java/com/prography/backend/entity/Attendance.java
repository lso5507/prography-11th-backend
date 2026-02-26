package com.prography.backend.entity;

import com.prography.backend.domain.AttendanceSource;
import com.prography.backend.domain.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_member_id")
    private CohortMember cohortMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private SessionEntity session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private int lateMinutes;

    @Column(nullable = false)
    private int penaltyAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceSource source;

    @Column
    private LocalDateTime checkedAt;

    @Column
    private String reason;

    protected Attendance() {
    }

    public Attendance(CohortMember cohortMember, SessionEntity session, AttendanceStatus status, Integer lateMinutes,
                      int penaltyAmount, AttendanceSource source, LocalDateTime checkedAt, String reason) {
        this.cohortMember = cohortMember;
        this.session = session;
        this.status = status;
        this.lateMinutes = lateMinutes == null ? 0 : lateMinutes;
        this.penaltyAmount = penaltyAmount;
        this.source = source;
        this.checkedAt = checkedAt;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public CohortMember getCohortMember() {
        return cohortMember;
    }

    public SessionEntity getSession() {
        return session;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public int getPenaltyAmount() {
        return penaltyAmount;
    }

    public AttendanceSource getSource() {
        return source;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public String getReason() {
        return reason;
    }

    public void update(AttendanceStatus status, Integer lateMinutes, int penaltyAmount, String reason) {
        this.status = status;
        this.lateMinutes = lateMinutes == null ? 0 : lateMinutes;
        this.penaltyAmount = penaltyAmount;
        if (reason != null) {
            this.reason = reason;
        }
    }
}
