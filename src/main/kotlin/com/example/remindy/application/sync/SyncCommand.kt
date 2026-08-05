package com.example.remindy.application.sync

import com.example.remindy.domain.reminder.Schedule
import com.example.remindy.domain.study.Frequency
import com.example.remindy.domain.study.StudyItemKind
import java.time.Instant
import java.util.UUID

data class SyncCommand(
    val lastSyncedAt: Instant?,
    val reminders: EntityChanges<ReminderChange>,
    val studyItems: EntityChanges<StudyItemChange>,
    val studyNotificationSetting: NotificationSettingChange?,
)

data class EntityChanges<T>(
    val upserted: List<T> = emptyList(),
    val deleted: List<DeletedItem> = emptyList(),
)

data class ReminderChange(
    val id: UUID,
    val title: String,
    val schedule: Schedule,
    val enabled: Boolean,
    val updatedAt: Instant,
)

data class StudyItemChange(
    val id: UUID,
    val kind: StudyItemKind,
    val prompt: String,
    val answer: String,
    val enabled: Boolean,
    val updatedAt: Instant,
)

data class NotificationSettingChange(
    val frequency: Frequency,
    val enabled: Boolean,
    val updatedAt: Instant,
)

data class DeletedItem(
    val id: UUID,
    val deletedAt: Instant,
)
