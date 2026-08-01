-- liquibase formatted sql
-- changeset backend-team:add-newsletter-delivery-columns

-- Publish previously reported only recipient_count, so an admin could not tell a successful
-- broadcast apart from one where every provider call failed.
ALTER TABLE users.newsletter_campaigns ADD COLUMN IF NOT EXISTS status VARCHAR(24) NOT NULL DEFAULT 'QUEUED';
ALTER TABLE users.newsletter_campaigns ADD COLUMN IF NOT EXISTS success_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users.newsletter_campaigns ADD COLUMN IF NOT EXISTS failure_count INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users.newsletter_campaigns ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

-- Per-subscriber token so newsletters can carry a one-click unsubscribe link that does not
-- require the recipient's email address to be passed around in a query string.
ALTER TABLE users.newsletter_subscribers ADD COLUMN IF NOT EXISTS unsubscribe_token UUID;

UPDATE users.newsletter_subscribers SET unsubscribe_token = gen_random_uuid() WHERE unsubscribe_token IS NULL;

ALTER TABLE users.newsletter_subscribers ALTER COLUMN unsubscribe_token SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_newsletter_subscribers_unsubscribe_token
    ON users.newsletter_subscribers(unsubscribe_token);

CREATE INDEX IF NOT EXISTS idx_newsletter_campaigns_created_at
    ON users.newsletter_campaigns(created_at DESC);
