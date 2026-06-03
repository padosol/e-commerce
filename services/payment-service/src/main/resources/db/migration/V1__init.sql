CREATE TABLE payment (
    id              UUID PRIMARY KEY,
    order_id        UUID           NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    idempotency_key VARCHAR(100)   NOT NULL UNIQUE,
    status          VARCHAR(16)    NOT NULL,
    transaction_id  VARCHAR(100),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);
