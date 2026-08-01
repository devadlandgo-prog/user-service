-- liquibase formatted sql
-- changeset backend-team:add-push-campaign-delivery-columns

-- The status column was sized for DRAFT|QUEUED|SENDING|SENT|FAILED. The delivery lifecycle now
-- also uses PROCESSING, PARTIAL, NO_RECIPIENTS and SCHEDULED.
ALTER TABLE push_campaigns ALTER COLUMN status TYPE VARCHAR(24);

-- Failure reason surfaced to the admin portal for FAILED campaigns.
ALTER TABLE push_campaigns ADD COLUMN IF NOT EXISTS error_message TEXT;

-- Number of times the worker has picked this campaign up; used to cap retries of stale jobs.
ALTER TABLE push_campaigns ADD COLUMN IF NOT EXISTS attempt_count INTEGER NOT NULL DEFAULT 0;

-- The worker polls by status on every tick.
CREATE INDEX IF NOT EXISTS idx_push_campaigns_status ON push_campaigns(status);

-- Recover campaigns that were left mid-flight by an earlier deploy in which the scheduler was
-- never enabled, so the new worker treats them as fresh work rather than stale in-flight jobs.
UPDATE push_campaigns SET status = 'QUEUED' WHERE status = 'SENDING';
