-- V2__hash_columns_binary32.sql
ALTER TABLE contact
    MODIFY COLUMN value_hash BINARY(32) NOT NULL;

ALTER TABLE participant
    MODIFY COLUMN contact_hash BINARY(32) NULL;

ALTER TABLE invitation
    MODIFY COLUMN contact_hash BINARY(32) NOT NULL,
    MODIFY COLUMN token_hash   BINARY(32) NOT NULL;
