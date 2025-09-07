CREATE TABLE users
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    oidc_sub     VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    updated_at   TIMESTAMP    NOT NULL
);