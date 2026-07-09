-- liquibase formatted sql
-- changeset backend-team:add-updated-at-to-newsletter

ALTER TABLE users.newsletter_campaigns ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users.newsletter_campaign_recipients ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
