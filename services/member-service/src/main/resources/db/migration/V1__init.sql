CREATE TABLE member (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL,
    nickname   VARCHAR(100) NOT NULL,
    grade      VARCHAR(16)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE member_address (
    id        UUID PRIMARY KEY,
    member_id UUID NOT NULL REFERENCES member(id) ON DELETE CASCADE,
    zipcode   VARCHAR(20)  NOT NULL,
    line1     VARCHAR(255) NOT NULL,
    line2     VARCHAR(255)
);
