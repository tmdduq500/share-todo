-- 1. 일정 수정 추적
ALTER TABLE event
    ADD COLUMN sequence INT NOT NULL DEFAULT 0,
    ADD COLUMN last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- 2. 리마인드
ALTER TABLE event
    ADD COLUMN reminder_minutes INT,
    ADD COLUMN reminder_sent BOOLEAN NOT NULL DEFAULT FALSE;

-- 3. 반복 일정 (RRULE 기반)
ALTER TABLE event
    ADD COLUMN rrule VARCHAR(500);

-- 인덱스 (리마인드 성능)
CREATE INDEX idx_event_reminder
    ON event (starts_at_utc, reminder_sent);