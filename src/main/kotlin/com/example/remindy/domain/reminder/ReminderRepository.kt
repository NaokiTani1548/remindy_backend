package com.example.remindy.domain.reminder

import com.example.remindy.domain.shared.UserId
import java.time.Instant

interface ReminderRepository {
    fun save(reminder: Reminder): Reminder
    fun findById(id: ReminderId): Reminder?
    /** アクティブ(論理削除されていない)なリマインダーを返す。 */
    fun findByUserId(userId: UserId): List<Reminder>
    fun deleteById(id: ReminderId)

    /** 指定時刻以降に更新されたリマインダーを返す(論理削除済みを含む)。同期用。 */
    fun findByUserIdModifiedSince(userId: UserId, since: Instant): List<Reminder>
    /** 同期用UPSERT。クライアントが生成したIDで挿入または更新する。 */
    fun upsert(reminder: Reminder, createdAt: Instant, updatedAt: Instant)
    /** 論理削除。 */
    fun softDelete(id: ReminderId, deletedAt: Instant)
}