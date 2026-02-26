package com.prography.backend.entity;

import com.prography.backend.domain.DepositType;
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

@Entity
public class DepositHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_member_id")
    private CohortMember cohortMember;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositType type;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private int balanceAfter;

    @Column
    private Long attendanceId;

    @Column(nullable = false)
    private String description;

    protected DepositHistory() {
    }

    public DepositHistory(CohortMember cohortMember, DepositType type, int amount, int balanceAfter, Long attendanceId,
                          String description) {
        this.cohortMember = cohortMember;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.attendanceId = attendanceId;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public CohortMember getCohortMember() {
        return cohortMember;
    }

    public DepositType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public Long getAttendanceId() {
        return attendanceId;
    }

    public String getDescription() {
        return description;
    }
}
