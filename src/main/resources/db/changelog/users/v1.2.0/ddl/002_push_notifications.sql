-- Device tokens (one user, many devices)
CREATE TABLE user_device_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fcm_token       VARCHAR(512) NOT NULL,
    platform        VARCHAR(16) NOT NULL,  -- WEB | ANDROID | IOS
    device_label    VARCHAR(128),
    app_version     VARCHAR(32),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (fcm_token)
);

CREATE INDEX idx_user_device_tokens_user_id ON user_device_tokens(user_id);
CREATE INDEX idx_user_device_tokens_active ON user_device_tokens(active) WHERE active = TRUE;

-- Push templates (admin-managed)
CREATE TABLE push_templates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title           VARCHAR(120) NOT NULL,
    body            VARCHAR(500) NOT NULL,
    image_url       VARCHAR(2048),
    deep_link       VARCHAR(512),          -- e.g. landgo://listings/123
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Push campaigns (send history)
CREATE TABLE push_campaigns (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id     UUID REFERENCES push_templates(id),
    title           VARCHAR(120) NOT NULL,
    body            VARCHAR(500) NOT NULL,
    image_url       VARCHAR(2048),
    deep_link       VARCHAR(512),
    audience        VARCHAR(32) NOT NULL,  -- ALL | BUYERS | SELLERS | VENDORS | SINGLE_USER
    audience_filter JSONB,                 -- e.g. {"userId":"..."} or {"plan":"Premium"}
    status          VARCHAR(16) NOT NULL,  -- DRAFT | QUEUED | SENDING | SENT | FAILED
    targeted_count  INTEGER NOT NULL DEFAULT 0,
    success_count   INTEGER NOT NULL DEFAULT 0,
    failure_count   INTEGER NOT NULL DEFAULT 0,
    sent_by         UUID REFERENCES users(id),
    scheduled_at    TIMESTAMPTZ,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE push_delivery_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id     UUID NOT NULL REFERENCES push_campaigns(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL,
    fcm_token       VARCHAR(512) NOT NULL,
    status          VARCHAR(16) NOT NULL,  -- SENT | FAILED | INVALID_TOKEN
    provider_msg_id VARCHAR(128),
    error_code      VARCHAR(64),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
