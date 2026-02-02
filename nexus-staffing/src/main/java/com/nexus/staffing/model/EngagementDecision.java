package com.nexus.staffing.model;

import com.nexus.staffing.model.enums.EngagementDecisionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "engagement_decision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngagementDecision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "engagement_id", nullable = false)
    private Long engagementId;

    @Column(name = "decided_by", nullable = false)
    private Long decidedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private EngagementDecisionType decision;

    @Column(name = "comment", length = 1000)
    private String comment;

    @Column(name = "decision_date", nullable = false)
    private LocalDateTime decisionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (this.decisionDate == null) {
            this.decisionDate = now;
        }

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}