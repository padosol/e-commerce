CREATE TABLE orders (
    id         UUID PRIMARY KEY,
    user_id    UUID         NOT NULL,
    status     VARCHAR(16)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE order_line (
    id         UUID PRIMARY KEY,
    order_id   UUID           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID           NOT NULL,
    quantity   INT            NOT NULL,
    unit_price NUMERIC(19, 2) NOT NULL
);

CREATE TABLE outbox_event (
    id             UUID PRIMARY KEY,
    aggregate_type VARCHAR(50)  NOT NULL,
    aggregate_id   UUID         NOT NULL,
    event_type     VARCHAR(50)  NOT NULL,
    payload        TEXT         NOT NULL,
    published      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_outbox_unpublished ON outbox_event (published, created_at);
