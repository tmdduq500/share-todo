create table device_token
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    account_id BIGINT       NOT NULL,
    token      VARCHAR(255) NOT NULL,
    platform   VARCHAR(20)  NOT NULL,
    active     BIT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_device_token_token UNIQUE (token),
    CONSTRAINT fk_device_token_account FOREIGN KEY (account_id) REFERENCES account (id)
);