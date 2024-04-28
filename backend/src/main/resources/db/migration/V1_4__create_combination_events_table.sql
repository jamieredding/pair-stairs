CREATE TABLE combination_events
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    date           DATE NOT NULL,
    combination_id BIGINT,
    FOREIGN KEY (combination_id) REFERENCES combinations (id)
);