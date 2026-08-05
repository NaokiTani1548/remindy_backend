package com.example.remindy.application.sync

import com.example.remindy.domain.reminder.*
import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class SyncUseCase(
    private val reminderRepository: ReminderRepository,
    private val studyItemRepository: StudyItemRepository,
    private val settingRepository: StudyNotificationSettingRepository,
    private val clock: Clock,
) {
    @Transactional
    fun handle(userId: UserId, command: SyncCommand): SyncResult {
        val syncTimestamp = Instant.now(clock)

        // 1. クライアントからのリマインダー変更を適用
        applyReminderChanges(userId, command.reminders, syncTimestamp)

        // 2. クライアントからの学習アイテム変更を適用
        applyStudyItemChanges(userId, command.studyItems, syncTimestamp)

        // 3. 通知設定の同期
        applyNotificationSettingChange(userId, command.studyNotificationSetting)

        // 4. サーバー側の変更を取得
        val lastSyncedAt = command.lastSyncedAt ?: Instant.EPOCH
        val serverReminders = reminderRepository.findByUserIdModifiedSince(userId, lastSyncedAt)
        val serverStudyItems = studyItemRepository.findByUserIdModifiedSince(userId, lastSyncedAt)
        val serverSetting = settingRepository.findByUserIdIfModifiedSince(userId, lastSyncedAt)

        // 5. upserted と deleted を分離して返却
        val (deletedReminders, activeReminders) = serverReminders.partition { it.isDeleted }
        val (deletedStudyItems, activeStudyItems) = serverStudyItems.partition { it.isDeleted }

        return SyncResult(
            reminders = EntitySyncResult(
                upserted = activeReminders,
                deleted = deletedReminders.mapNotNull { it.id?.value },
            ),
            studyItems = EntitySyncResult(
                upserted = activeStudyItems,
                deleted = deletedStudyItems.mapNotNull { it.id?.value },
            ),
            studyNotificationSetting = serverSetting,
            syncedAt = syncTimestamp,
        )
    }

    private fun applyReminderChanges(userId: UserId, changes: EntityChanges<ReminderChange>, now: Instant) {
        for (change in changes.upserted) {
            val id = ReminderId(change.id)
            val existing = reminderRepository.findById(id)
            if (existing != null && existing.userId != userId) continue

            if (existing == null || change.updatedAt.isAfter(existing.deletedAt ?: Instant.EPOCH)) {
                val reminder = Reminder.reconstitute(
                    id = id,
                    userId = userId,
                    title = ReminderTitle.of(change.title),
                    schedule = change.schedule,
                    enabled = change.enabled,
                )
                val createdAt = if (existing == null) change.updatedAt else change.updatedAt
                reminderRepository.upsert(reminder, createdAt = createdAt, updatedAt = change.updatedAt)
            }
        }

        for (deletion in changes.deleted) {
            val id = ReminderId(deletion.id)
            val existing = reminderRepository.findById(id)
            if (existing != null && existing.userId == userId && !existing.isDeleted) {
                reminderRepository.softDelete(id, deletion.deletedAt)
            }
        }
    }

    private fun applyStudyItemChanges(userId: UserId, changes: EntityChanges<StudyItemChange>, now: Instant) {
        for (change in changes.upserted) {
            val id = StudyItemId(change.id)
            val existing = studyItemRepository.findById(id)
            if (existing != null && existing.userId != userId) continue

            if (existing == null || change.updatedAt.isAfter(existing.deletedAt ?: Instant.EPOCH)) {
                val item = StudyItem.reconstitute(
                    id = id,
                    userId = userId,
                    kind = change.kind,
                    prompt = Prompt.of(change.prompt),
                    answer = Answer.of(change.answer),
                    enabled = change.enabled,
                )
                val createdAt = if (existing == null) change.updatedAt else change.updatedAt
                studyItemRepository.upsert(item, createdAt = createdAt, updatedAt = change.updatedAt)
            }
        }

        for (deletion in changes.deleted) {
            val id = StudyItemId(deletion.id)
            val existing = studyItemRepository.findById(id)
            if (existing != null && existing.userId == userId && !existing.isDeleted) {
                studyItemRepository.softDelete(id, deletion.deletedAt)
            }
        }
    }

    private fun applyNotificationSettingChange(userId: UserId, change: NotificationSettingChange?) {
        if (change == null) return
        val existing = settingRepository.findByUserId(userId)
        if (existing == null) {
            val setting = StudyNotificationSetting.reconstitute(userId, change.frequency, change.enabled)
            settingRepository.save(setting)
        } else {
            settingRepository.save(existing.update(change.frequency, change.enabled))
        }
    }
}
