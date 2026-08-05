-- ソフトデリート対応: deleted_at カラムを追加
ALTER TABLE reminders ADD COLUMN deleted_at timestamptz;
ALTER TABLE study_items ADD COLUMN deleted_at timestamptz;

-- アクティブアイテムの検索用パーシャルインデックス
CREATE INDEX idx_reminders_user_active ON reminders (user_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_study_items_user_active ON study_items (user_id) WHERE deleted_at IS NULL;

-- 同期用: updated_at 以降の変更を取得するインデックス
CREATE INDEX idx_reminders_user_updated ON reminders (user_id, updated_at);
CREATE INDEX idx_study_items_user_updated ON study_items (user_id, updated_at);
