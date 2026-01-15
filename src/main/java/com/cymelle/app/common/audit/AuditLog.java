package com.cymelle.app.common.audit;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="actor_id")
    private Long actorId;

    @Column(name="actor_role")
    private String actorRole;

    @Column(nullable=false)
    private String action;

    @Column(name="entity_type", nullable=false)
    private String entityType;

    @Column(name="entity_id")
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
