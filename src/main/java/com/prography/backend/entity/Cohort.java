package com.prography.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cohort extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int generation;

    @Column(nullable = false)
    private boolean current;

    protected Cohort() {
    }

    public Cohort(String name, int generation, boolean current) {
        this.name = name;
        this.generation = generation;
        this.current = current;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getGeneration() {
        return generation;
    }

    public boolean isCurrent() {
        return current;
    }
}
