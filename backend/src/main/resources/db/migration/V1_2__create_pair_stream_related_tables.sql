CREATE TABLE pair_streams
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_id BIGINT NOT NULL,
    FOREIGN KEY (stream_id) REFERENCES streams (id)
);

CREATE TABLE developer_pair_member
(
    pair_id      BIGINT NOT NULL,
    developer_id BIGINT NOT NULL,
    PRIMARY KEY (pair_id, developer_id),
    FOREIGN KEY (pair_id) REFERENCES pair_streams (id),
    FOREIGN KEY (developer_id) REFERENCES developers (id)
);
