package com.example.remindy.application.sync

import com.example.remindy.domain.reminder.Reminder
import com.example.remindy.domain.study.StudyItem
import com.example.remindy.domain.study.StudyNotificationSetting
import java.time.Instant
import java.util.UUID

data class SyncResult(
    val reminders: EntitySyncResult<Reminder>,
    val studyItems: EntitySyncResult<StudyItem>,
    val studyNotificationSetting: StudyNotificationSetting?,
    val syncedAt: Instant,
)

data class EntitySyncResult<T>(
    val upserted: List<T>,
    val deleted: List<UUID>,
)
