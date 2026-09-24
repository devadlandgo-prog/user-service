SET search_path TO users;

-- Every transactional email the platform sends passes through user-service, so
-- deduplication lives in one table rather than being reimplemented per caller.
-- The unique key is the committed business event (payment id, listing status
-- version, verification token) — a webhook replay or a client retry reuses it
-- and delivers nothing.
CREATE TABLE IF NOT EXISTS email_deliveries (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(200) NOT NULL,
    to_email VARCHAR(320) NOT NULL,
    subject VARCHAR(500),
    template_name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    -- Provider error text only. Never the rendered body, a verification code or
    -- a reset token.
    last_error TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMP,
    CONSTRAINT email_deliveries_idempotency_key_key UNIQUE (idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_email_deliveries_status
    ON email_deliveries (status, created_at DESC);
