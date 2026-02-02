-- share-todo V1 (MariaDB / InnoDB / utf8mb4 / UTC)
SET NAMES utf8mb4;
SET time_zone = '+00:00';

-- 1) Account: 로그인 가능 주체(회원). 외부 노출용 uid(ULID, 26자) 병행
CREATE TABLE IF NOT EXISTS account (
                                       id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       uid             CHAR(26) NOT NULL UNIQUE,          -- 공개용 ULID
                                       email_norm      VARCHAR(255) UNIQUE NULL,          -- 정규화 이메일(선택)
                                       phone_norm      VARCHAR(32)  UNIQUE NULL,          -- E.164(선택)
                                       password_hash   VARBINARY(255) NULL,
                                       provider        VARCHAR(32)  NOT NULL DEFAULT 'local', -- local/google/apple 등
                                       status          VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
                                       created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                       updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                       last_login_at   DATETIME(6) NULL,
                                       CONSTRAINT chk_account_status CHECK (status IN ('ACTIVE','INACTIVE','LOCKED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2) Person: 일정 참여 주체(회원/비회원 모두). 회원이면 account_id로 연결
CREATE TABLE IF NOT EXISTS person (
                                      id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      uid             CHAR(26) NOT NULL UNIQUE,          -- 공개용 ULID
                                      account_id      BIGINT NULL,
                                      display_name    VARCHAR(100) NULL,
                                      created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                      updated_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                      CONSTRAINT fk_person_account FOREIGN KEY (account_id) REFERENCES account(id)
                                          ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_person_account ON person(account_id);

-- 3) Contact: 연락처(PII 최소화). value_hash 중심으로 중복/매칭
CREATE TABLE IF NOT EXISTS contact (
                                       id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                                       person_id       BIGINT NOT NULL,
                                       channel         VARCHAR(10) NOT NULL,              -- EMAIL | PHONE
                                       value_norm      VARCHAR(255) NULL,                 -- 필요 시 암호화 저장
                                       value_hash      VARBINARY(64) NOT NULL,            -- SHA-256 등 (salt/pepper 적용)
                                       verified_at     DATETIME(6) NULL,
                                       created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                       updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                       UNIQUE KEY uq_contact_channel_hash (channel, value_hash),
                                       KEY idx_contact_person (person_id),
                                       CONSTRAINT fk_contact_person FOREIGN KEY (person_id) REFERENCES person(id)
                                           ON UPDATE CASCADE ON DELETE CASCADE,
                                       CONSTRAINT chk_contact_channel CHECK (channel IN ('EMAIL','PHONE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4) Event: 일정(UTC 저장, 외부 노출 uid)
CREATE TABLE IF NOT EXISTS event (
                                     id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     uid                CHAR(26) NOT NULL UNIQUE,
                                     owner_person_id    BIGINT NOT NULL,
                                     title              VARCHAR(200) NOT NULL,
                                     description        LONGTEXT NULL,
                                     starts_at_utc      DATETIME(6) NOT NULL,
                                     ends_at_utc        DATETIME(6) NOT NULL,
                                     timezone           VARCHAR(64) NULL,               -- 생성 시점 표기용
                                     location           VARCHAR(255) NULL,
                                     all_day            TINYINT(1) NOT NULL DEFAULT 0,
                                     visibility         VARCHAR(10) NOT NULL DEFAULT 'PRIVATE', -- PRIVATE | LINK
                                     created_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                     updated_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                     CONSTRAINT fk_event_owner FOREIGN KEY (owner_person_id) REFERENCES person(id)
                                         ON UPDATE CASCADE ON DELETE RESTRICT,
                                     CONSTRAINT chk_event_visibility CHECK (visibility IN ('PRIVATE','LINK')),
                                     CONSTRAINT chk_event_time CHECK (ends_at_utc > starts_at_utc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_owner ON event(owner_person_id);
CREATE INDEX idx_event_starts ON event(starts_at_utc);

-- 5) Participant: 일정-사람 관계(초대/수락 상태). 비회원 수락 전에는 person_id NULL+contact_hash로 홀딩
CREATE TABLE IF NOT EXISTS participant (
                                           id               BIGINT PRIMARY KEY AUTO_INCREMENT,
                                           event_id         BIGINT NOT NULL,
                                           person_id        BIGINT NULL,
                                           contact_hash     VARBINARY(64) NULL,               -- 초대 시 연락처 해시(비회원 수락 전)
                                           role             VARCHAR(10) NOT NULL DEFAULT 'ATTENDEE',  -- OWNER | ATTENDEE
                                           status           VARCHAR(12) NOT NULL DEFAULT 'INVITED',   -- INVITED | ACCEPTED | DECLINED | REMOVED
                                           created_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                           updated_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                           UNIQUE KEY uq_participant_event_person (event_id, person_id),
                                           UNIQUE KEY uq_participant_event_contact (event_id, contact_hash),
                                           KEY idx_participant_event (event_id),
                                           CONSTRAINT fk_participant_event FOREIGN KEY (event_id) REFERENCES event(id)
                                               ON UPDATE CASCADE ON DELETE CASCADE,
                                           CONSTRAINT fk_participant_person FOREIGN KEY (person_id) REFERENCES person(id)
                                               ON UPDATE CASCADE ON DELETE SET NULL,
                                           CONSTRAINT chk_participant_role CHECK (role IN ('OWNER','ATTENDEE')),
                                           CONSTRAINT chk_participant_status CHECK (status IN ('INVITED','ACCEPTED','DECLINED','REMOVED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6) Invitation: 개별 초대(토큰/만료/채널). 토큰 원문 저장 금지→해시 저장
CREATE TABLE IF NOT EXISTS invitation (
                                          id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          uid                CHAR(26) NOT NULL UNIQUE,       -- 초대 자체의 공개용 ULID
                                          event_id           BIGINT NOT NULL,
                                          inviter_person_id  BIGINT NOT NULL,
                                          channel            VARCHAR(10) NOT NULL,           -- EMAIL | PHONE
                                          contact_hash       VARBINARY(64) NOT NULL,
                                          token_hash         VARBINARY(64) NOT NULL,         -- Base62/UUID 토큰의 SHA-256 해시
                                          expires_at         DATETIME(6) NOT NULL,
                                          accepted_at        DATETIME(6) NULL,
                                          created_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                          updated_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                          KEY idx_invitation_event (event_id),
                                          UNIQUE KEY uq_invitation_unique (event_id, channel, contact_hash, accepted_at),
                                          CONSTRAINT fk_invitation_event FOREIGN KEY (event_id) REFERENCES event(id)
                                              ON UPDATE CASCADE ON DELETE CASCADE,
                                          CONSTRAINT fk_invitation_inviter FOREIGN KEY (inviter_person_id) REFERENCES person(id)
                                              ON UPDATE CASCADE ON DELETE RESTRICT,
                                          CONSTRAINT chk_invitation_channel CHECK (channel IN ('EMAIL','PHONE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8) Notification: 발송 로그(SES/Twilio/SENS 등)
CREATE TABLE IF NOT EXISTS notification (
                                            id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            invitation_id      BIGINT NOT NULL,
                                            provider           VARCHAR(20) NOT NULL,       -- SES | TWILIO | SENS ...
                                            request_payload    LONGTEXT NULL,
                                            response_payload   LONGTEXT NULL,
                                            status             VARCHAR(10) NOT NULL DEFAULT 'PENDING', -- PENDING | SENT | FAILED
                                            retry_count        INT NOT NULL DEFAULT 0,
                                            created_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                                            updated_at         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                                            KEY idx_notification_invitation (invitation_id),
                                            KEY idx_notification_created (created_at),
                                            CONSTRAINT fk_notification_invitation FOREIGN KEY (invitation_id) REFERENCES invitation(id)
                                                ON UPDATE CASCADE ON DELETE CASCADE,
                                            CONSTRAINT chk_notification_status CHECK (status IN ('PENDING','SENT','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
