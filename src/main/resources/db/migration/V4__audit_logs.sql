CREATE TABLE audit_logs (
                            id BIGSERIAL PRIMARY KEY,
                            actor_id BIGINT,
                            actor_role VARCHAR(20),
                            action VARCHAR(60) NOT NULL,
                            entity_type VARCHAR(40) NOT NULL,
                            entity_id BIGINT,
                            metadata TEXT,
                            created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
