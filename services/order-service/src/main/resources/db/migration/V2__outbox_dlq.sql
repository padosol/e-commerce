-- Outbox 재시도/DLQ: 발행 시도 횟수와 사망(dead-letter) 표시 컬럼
ALTER TABLE outbox_event ADD COLUMN attempts INT NOT NULL DEFAULT 0;
ALTER TABLE outbox_event ADD COLUMN dead BOOLEAN NOT NULL DEFAULT FALSE;

-- 발행 대상 조회 인덱스 갱신
DROP INDEX IF EXISTS idx_outbox_unpublished;
CREATE INDEX idx_outbox_pending ON outbox_event (published, dead, created_at);
