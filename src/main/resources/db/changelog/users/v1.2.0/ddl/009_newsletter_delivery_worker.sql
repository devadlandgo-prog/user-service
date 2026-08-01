-- liquibase formatted sql
-- changeset backend-team:add-newsletter-delivery-worker-columns

-- Newsletter delivery moved off the publish request thread onto a queue worker, mirroring the
-- push campaign pipeline. These columns track an in-flight run so a campaign can be claimed,
-- recovered after a crash, and reported on.
ALTER TABLE users.newsletter_campaigns ADD COLUMN IF NOT EXISTS started_at TIMESTAMP;
ALTER TABLE users.newsletter_campaigns ADD COLUMN IF NOT EXISTS attempt_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users.newsletter_campaigns ADD COLUMN IF NOT EXISTS error_message TEXT;

-- The worker polls by status on every tick.
CREATE INDEX IF NOT EXISTS idx_newsletter_campaigns_status ON users.newsletter_campaigns(status);

-- Resuming a partially-sent campaign looks up which subscribers already received it.
CREATE INDEX IF NOT EXISTS idx_newsletter_recipients_campaign_status
    ON users.newsletter_campaign_recipients(campaign_id, status);
