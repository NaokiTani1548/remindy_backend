-- =====================================================================
-- R__seed_local_data : ローカル開発用のモックデータ（local プロファイル限定）
--   - 反復実行(R__)。ON CONFLICT DO NOTHING で冪等に。
--   - id は固定 UUID を明示指定（DEFAULT uuidv7 を上書き）。FK を確実に張るため。
--   - created_at/updated_at はシードなので now() を直接使用（アプリの Clock を通らない割り切り）。
--   - password_hash は開発用の {noop} プレフィックス（後述）。本物データには絶対に使わないこと。
-- =====================================================================

-- ---------- users ----------
INSERT INTO users (id, email_address, password_hash, created_at, updated_at) VALUES
   ('11111111-1111-1111-1111-111111111111', 'alice@example.com', '{noop}devpass', now(), now()),
   ('22222222-2222-2222-2222-222222222222', 'bob@example.com',   '{noop}devpass', now(), now())
ON CONFLICT (id) DO NOTHING;

-- ---------- reminders （4種すべてを網羅して CHECK 制約も確認） ----------
INSERT INTO reminders
(id, user_id, title, schedule_type, schedule_time, schedule_date, schedule_day_of_week, schedule_day_of_month, enabled, created_at, updated_at) VALUES
    -- ONE_TIME: date + time
    ('a0000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
        '資料を送る', 'ONE_TIME', '09:00', '2026-07-10', NULL, NULL, true, now(), now()),
    -- DAILY: time のみ
    ('a0000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
        '水を飲む', 'DAILY', '10:30', NULL, NULL, NULL, true, now(), now()),
    -- WEEKLY: dayOfWeek + time
    ('a0000000-0000-0000-0000-000000000003', '11111111-1111-1111-1111-111111111111',
        '週次ミーティング', 'WEEKLY', '13:00', NULL, 'MONDAY', NULL, true, now(), now()),
    -- MONTHLY: dayOfMonth + time（無効フラグの例も兼ねる）
    ('a0000000-0000-0000-0000-000000000004', '22222222-2222-2222-2222-222222222222',
        '家賃の支払い', 'MONTHLY', '08:00', NULL, NULL, 25, false, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ---------- study_items （QA と TERM の両方） ----------
INSERT INTO study_items (id, user_id, kind, prompt, answer, enabled, created_at, updated_at) VALUES
    ('b0000000-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111',
    'QA',   'HTTP 404 の意味は？', 'リソースが見つからない', true, now(), now()),
    ('b0000000-0000-0000-0000-000000000002', '11111111-1111-1111-1111-111111111111',
    'TERM', '冪等性', '同じ操作を複数回行っても結果が変わらない性質', true, now(), now())
ON CONFLICT (id) DO NOTHING;

-- ---------- study_notification_settings （1ユーザー1件） ----------
INSERT INTO study_notification_settings (user_id, frequency, enabled, updated_at) VALUES
    ('11111111-1111-1111-1111-111111111111', 'THREE_TIMES', true,  now()),
    ('22222222-2222-2222-2222-222222222222', 'ONCE',        false, now())
ON CONFLICT (user_id) DO NOTHING;