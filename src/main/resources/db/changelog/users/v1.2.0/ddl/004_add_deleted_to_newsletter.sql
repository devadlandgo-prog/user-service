-- liquibase formatted sql
-- changeset backend-team:add-deleted-to-newsletter

ALTER TABLE users.newsletter_subscribers ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users.newsletter_campaigns ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users.newsletter_campaign_recipients ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
