CREATE TABLE combinations
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE combination_pair_member
(
    combination_id      BIGINT NOT NULL,
    pair_stream_id BIGINT NOT NULL,
    PRIMARY KEY (combination_id, pair_stream_id),
    FOREIGN KEY (combination_id) REFERENCES combinations (id),
    FOREIGN KEY (pair_stream_id) REFERENCES pair_streams (id)
);
