-- =====================================================================
-- V1: 初期スキーマ
--   集約: users / reminders / study_items / study_notification_settings
--   方針:
--     - 列挙値は varchar + CHECK 制約
--     - 所有関係は user_id FK + ON DELETE CASCADE
--     - サロゲート PK は DB が uuidv7() で採番（時刻順で B-tree に優しい, PG18 組み込み）
--     - 監査時刻(created_at/updated_at)はアプリが Clock でセット。DB の DEFAULT now() は置かない
--     - email の大小非区別は EmailAddress 値オブジェクトで小文字正規化して担保。
--       DB は素直な UNIQUE(email_address) で一意性のみ守る
-- =====================================================================

-- ---------- users ----------
CREATE TABLE users (
                       id            uuid          PRIMARY KEY DEFAULT uuidv7(),   -- DB 採番（v7）
                       email_address varchar(254)  NOT NULL,                       -- VO 側で小文字正規化済み。254 は RFC 上限
                       password_hash varchar(255)  NOT NULL,                       -- bcrypt 60字 + {bcrypt}接頭辞/将来変更に余裕
                       created_at    timestamptz   NOT NULL,
                       updated_at    timestamptz   NOT NULL,
                       CONSTRAINT uq_users_email UNIQUE (email_address)
);

-- ---------- reminders ----------
CREATE TABLE reminders (
                           id                    uuid         PRIMARY KEY DEFAULT uuidv7(),   -- DB 採番（v7）
                           user_id               uuid         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                           title                 varchar(100) NOT NULL,
                           schedule_type         varchar(10)  NOT NULL,
                           schedule_time         time         NOT NULL,        -- 4種すべてが持つ壁時計時刻(JST)
                           schedule_date         date,                         -- ONE_TIME のみ
                           schedule_day_of_week  varchar(9),                   -- WEEKLY のみ
                           schedule_day_of_month smallint,                     -- MONTHLY のみ
                           enabled               boolean      NOT NULL DEFAULT true,
                           created_at            timestamptz  NOT NULL,
                           updated_at            timestamptz  NOT NULL,
                           CONSTRAINT ck_reminders_type
                               CHECK (schedule_type IN ('ONE_TIME','DAILY','WEEKLY','MONTHLY')),
                           CONSTRAINT ck_reminders_dow
                               CHECK (schedule_day_of_week IS NULL OR schedule_day_of_week IN
                                                                      ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
                           CONSTRAINT ck_reminders_dom
                               CHECK (schedule_day_of_month IS NULL OR schedule_day_of_month BETWEEN 1 AND 31),
    -- 直和型の形状を強制：type ごとに「使う列＝NOT NULL／使わない列＝NULL」を固定
                           CONSTRAINT ck_reminders_schedule_shape CHECK (
                               (schedule_type = 'ONE_TIME' AND schedule_date IS NOT NULL AND schedule_day_of_week IS NULL     AND schedule_day_of_month IS NULL)
                                   OR (schedule_type = 'DAILY'    AND schedule_date IS NULL     AND schedule_day_of_week IS NULL     AND schedule_day_of_month IS NULL)
                                   OR (schedule_type = 'WEEKLY'   AND schedule_date IS NULL     AND schedule_day_of_week IS NOT NULL AND schedule_day_of_month IS NULL)
                                   OR (schedule_type = 'MONTHLY'  AND schedule_date IS NULL     AND schedule_day_of_week IS NULL     AND schedule_day_of_month IS NOT NULL)
                               )
);
CREATE INDEX idx_reminders_user         ON reminders (user_id);
CREATE INDEX idx_reminders_user_enabled ON reminders (user_id, enabled);

-- ---------- study_items ----------
CREATE TABLE study_items (
                             id         uuid         PRIMARY KEY DEFAULT uuidv7(),   -- DB 採番（v7）
                             user_id    uuid         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
                             kind       varchar(4)   NOT NULL,
                             prompt     varchar(500) NOT NULL,                  -- 表（質問・単語）
                             answer     text         NOT NULL,                  -- 裏（回答・詳細）
                             enabled    boolean      NOT NULL DEFAULT true,
                             created_at timestamptz  NOT NULL,
                             updated_at timestamptz  NOT NULL,
                             CONSTRAINT ck_study_items_kind CHECK (kind IN ('QA','TERM'))
);
CREATE INDEX idx_study_items_user         ON study_items (user_id);
CREATE INDEX idx_study_items_user_enabled ON study_items (user_id, enabled);

-- ---------- study_notification_settings ----------
-- 注意: この user_id は「採番される代理キー」ではなく users への参照そのもの。
--       アプリが所有ユーザーの id を入れるため DEFAULT uuidv7() は付けない。
CREATE TABLE study_notification_settings (
                                             user_id    uuid        PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE, -- PK=FK で1ユーザー1件を構造保証
                                             frequency  varchar(11) NOT NULL,
                                             enabled    boolean     NOT NULL DEFAULT true,
                                             updated_at timestamptz NOT NULL,
                                             CONSTRAINT ck_study_notif_frequency CHECK (frequency IN ('ONCE','THREE_TIMES','FIVE_TIMES'))
    -- 時間帯(9–21時)は固定定数のため列に持たない
);