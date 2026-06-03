CREATE TABLE shipment (
    id              UUID PRIMARY KEY,
    order_id        UUID         NOT NULL,
    zipcode         VARCHAR(20)  NOT NULL,
    line1           VARCHAR(255) NOT NULL,
    line2           VARCHAR(255),
    status          VARCHAR(16)  NOT NULL,
    tracking_number VARCHAR(100),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
