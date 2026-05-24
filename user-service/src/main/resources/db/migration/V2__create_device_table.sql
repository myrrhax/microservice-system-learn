CREATE TABLE device
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    type     VARCHAR(50)  NOT NULL,
    location VARCHAR(255),
    user_id  BIGINT       NOT NULL REFERENCES users ON DELETE CASCADE
);