package com.prography.backend.entity;

import com.prography.backend.domain.SessionStatus;
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
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class SessionEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id")
    private Cohort cohort;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    protected SessionEntity() {
    }

    public SessionEntity(Cohort cohort, String title, LocalDate sessionDate, LocalTime startTime, LocalTime endTime, String location,
                         SessionStatus status) {
        this.cohort = cohort;
        this.title = title;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Cohort getCohort() {
        return cohort;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void update(String title, LocalDate sessionDate, LocalTime startTime, String location, SessionStatus status) {
        if (title != null) {
            this.title = title;
        }
        if (sessionDate != null) {
            this.sessionDate = sessionDate;
        }
        if (startTime != null) {
            this.startTime = startTime;
            this.endTime = startTime;
        }
        if (location != null) {
            this.location = location;
        }
        if (status != null) {
            this.status = status;
        }
    }

    public void cancel() {
        this.status = SessionStatus.CANCELLED;
    }
}
