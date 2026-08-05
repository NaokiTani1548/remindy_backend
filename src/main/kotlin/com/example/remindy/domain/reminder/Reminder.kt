package com.example.remindy.domain.reminder

import com.example.remindy.domain.shared.UserId
import java.time.Instant

/**
 * リマインダー集約のルート。
 *
 * ・id は ReminderId?。null は「まだ永続化されていない(DB採番前)」を意味する。
 * ・不変(immutable)に保ち、状態変更は新しいインスタンスを返すメソッドで表す。
 * ・created_at/updated_at は監査メタデータとして"あえて持たない"(application層でClock管理)。
 * ・所有者の参照は UserId(識別子)のみ。User 集約への直接参照は持たない。
 * ・deletedAt が non-null のとき論理削除済み。同期でクライアントに削除を伝播するために保持する。
 */
class Reminder private constructor(
    val id: ReminderId?,
    val userId: UserId,
    val title: ReminderTitle,
    val schedule: Schedule,
    val enabled: Boolean,
    val deletedAt: Instant? = null,
) {
    val isPersisted: Boolean get() = id != null
    val isDeleted: Boolean get() = deletedAt != null

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
            deletedAt: Instant? = null,
        ): Reminder = Reminder(id, userId, title, schedule, enabled, deletedAt)
    }

    /** 内容(タイトル・スケジュール)の置換。PUT /reminders/{id} に対応。 */
    fun changeContent(newTitle: ReminderTitle, newSchedule: Schedule): Reminder =
        Reminder(id, userId, newTitle, newSchedule, enabled, deletedAt)

    /** 有効化 / 無効化。PATCH /reminders/{id} に対応。冪等。 */
    fun enable(): Reminder = if (enabled) this else Reminder(id, userId, title, schedule, true, deletedAt)
    fun disable(): Reminder = if (!enabled) this else Reminder(id, userId, title, schedule, false, deletedAt)

    /** 論理削除。 */
    fun softDelete(now: Instant): Reminder =
        if (isDeleted) this else Reminder(id, userId, title, schedule, enabled, now)
}