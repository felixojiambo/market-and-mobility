CREATE TABLE idempotency_keys (
                                  id BIGSERIAL PRIMARY KEY,
                                  user_id BIGINT NOT NULL,
                                  idem_key VARCHAR(128) NOT NULL,
                                  request_hash VARCHAR(64) NOT NULL,
                                  order_id BIGINT NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW(),

                                  CONSTRAINT uq_user_idem UNIQUE (user_id, idem_key)
);

CREATE INDEX idx_idem_user ON idempotency_keys(user_id);
CREATE INDEX idx_idem_order ON idempotency_keys(order_id);
