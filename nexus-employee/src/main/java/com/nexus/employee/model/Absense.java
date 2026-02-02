package com.nexus.employee.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "absense")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Absense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "start", nullable = false)
    private LocalDate start;

    @Column(name = "\"end\"", nullable = false)
    private LocalDate end;

    @Column(name = "approved", nullable = false)
    private boolean approved;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}