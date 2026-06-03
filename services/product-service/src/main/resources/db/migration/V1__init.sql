CREATE TABLE product (
    id             UUID PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    price_amount   NUMERIC(19, 2) NOT NULL,
    price_currency VARCHAR(3)   NOT NULL,
    category       VARCHAR(100) NOT NULL,
    status         VARCHAR(16)  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
