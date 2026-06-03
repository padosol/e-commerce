CREATE TABLE stock (
    product_id UUID PRIMARY KEY,
    available  INT    NOT NULL,
    reserved   INT    NOT NULL,
    version    BIGINT NOT NULL DEFAULT 0
);
