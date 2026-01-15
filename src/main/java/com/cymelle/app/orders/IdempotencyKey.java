package com.cymelle.app.orders;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "idempotency_keys",
        uniqueConstraints = @UniqueConstraint(name = "uq_user_idem", columnNames = {"user_id", "idem_key"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private Long userId;

    @Column(name="idem_key", nullable=false, length=128)
    private String idemKey;

    @Column(name="request_hash", nullable=false, length=64)
    private String requestHash;

    @Column(name="order_id", nullable=false)
    private Long orderId;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
