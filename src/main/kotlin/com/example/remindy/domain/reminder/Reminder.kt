package com.example.remindy.domain.reminder

import com.example.remindy.domain.shared.UserId

/**
 * リマインダー集約のルート。
 *
 * ・id は ReminderId?。null は「まだ永続化されていない(DB採番前)」を意味する。
 * ・不変(immutable)に保ち、状態変更は新しいインスタンスを返すメソッドで表す。
 * ・created_at/updated_at は監査メタデータとして"あえて持たない"(application層でClock管理)。
 * ・所有者の参照は UserId(識別子)のみ。User 集約への直接参照は持たない。
 */
class Reminder private constructor(
    val id: ReminderId?,
    val userId: UserId,
    val title: ReminderTitle,
    val schedule: Schedule,
    val enabled: Boolean,
) {
    val isPersisted: Boolean get() = id != null

    companion object {
        /** 新規作成。識別子はまだ無く、有効フラグは true を起点とする。 */
        fun create(userId: UserId, title: ReminderTitle, schedule: Schedule): Reminder =
            Reminder(id = null, userId = userId, title = title, schedule = schedule, enabled = true)

        /** 永続化層からの再構築。全状態が確定している前提。 */
        fun reconstitute(
            id: ReminderId,
            userId: UserId,
            title: ReminderTitle,
            schedule: Schedule,
            enabled: Boolean,
        ): Reminder = Reminder(id, userId, title, schedule, enabled)
    }

    /** 内容(タイトル・スケジュール)の置換。PUT /reminders/{id} に対応。 */
    fun changeContent(newTitle: ReminderTitle, newSchedule: Schedule): Reminder =
        Reminder(id, userId, newTitle, newSchedule, enabled)

    /** 有効化 / 無効化。PATCH /reminders/{id} に対応。冪等。 */
    fun enable(): Reminder = if (enabled) this else Reminder(id, userId, title, schedule, true)
    fun disable(): Reminder = if (!enabled) this else Reminder(id, userId, title, schedule, false)

    // equals/hashCode は"あえて"オーバーライドしない(= 参照同一性)。
    // エンティティの同一性は本来 id で判定すべきだが、id=null(未永続)の期間があるため
    // id基準にすると「保存前後で等価性が変わる」不整合が生じる。必要な箇所で .id を明示比較する方針。
}