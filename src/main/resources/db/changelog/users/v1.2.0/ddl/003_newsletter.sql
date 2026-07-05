-- liquibase formatted sql
-- changeset backend-team:add-newsletter-tables

CREATE TABLE users.newsletter_subscribers (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    consent BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users.newsletter_campaigns (
    id UUID PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    html_body TEXT NOT NULL,
    text_body TEXT,
    preview_text VARCHAR(255),
    recipient_count INTEGER NOT NULL DEFAULT 0,
    sent_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users.newsletter_campaign_recipients (
    id UUID PRIMARY KEY,
    campaign_id UUID NOT NULL REFERENCES users.newsletter_campaigns(id) ON DELETE CASCADE,
    subscriber_id UUID NOT NULL REFERENCES users.newsletter_subscribers(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'SENT',
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
