package com.example.remindy.presentation.rest.sync

import com.example.remindy.application.sync.*
import com.example.remindy.domain.study.Frequency
import com.example.remindy.domain.study.StudyItemKind
import com.example.remindy.presentation.rest.reminder.ReminderResponse
import com.example.remindy.presentation.rest.reminder.ScheduleDto
import com.example.remindy.presentation.rest.study.NotificationSettingResponse
import com.example.remindy.presentation.rest.study.StudyItemResponse
import jakarta.validation.Valid
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

// --- GET /api/v1/sync (既存: フルスナップショット) ---

data class SyncResponse(
    val reminders: List<ReminderResponse>,
    val studyItems: List<StudyItemResponse>,
    val studyNotificationSetting: NotificationSettingResponse,
    val serverTime: OffsetDateTime,
) {
    companion object {
        fun from(snapshot: SyncSnapshot) = SyncResponse(
            reminders = snapshot.reminders.map(ReminderResponse::from),
            studyItems = snapshot.studyItems.map(StudyItemResponse::from),
            studyNotificationSetting = NotificationSettingResponse.from(snapshot.studyNotificationSetting),
            serverTime = snapshot.serverTime,
        )
    }
}

// --- POST /api/v1/sync (新規: 双方向デルタ同期) ---

data class SyncPushRequest(
    val lastSyncedAt: Instant?,
    @field:Valid val reminders: SyncEntityRequest<ReminderSyncDto> = SyncEntityRequest(),
    @field:Valid val studyItems: SyncEntityRequest<StudyItemSyncDto> = SyncEntityRequest(),
    val studyNotificationSetting: NotificationSettingSyncDto? = null,
) {
    fun toCommand() = SyncCommand(
        lastSyncedAt = lastSyncedAt,
        reminders = EntityChanges(
            upserted = reminders.upserted.map { it.toChange() },
            deleted = reminders.deleted.map { DeletedItem(it.id, it.deletedAt) },
        ),
        studyItems = EntityChanges(
            upserted = studyItems.upserted.map { it.toChange() },
            deleted = studyItems.deleted.map { DeletedItem(it.id, it.deletedAt) },
        ),
        studyNotificationSetting = studyNotificationSetting?.toChange(),
    )
}

data class SyncEntityRequest<T>(
    val upserted: List<T> = emptyList(),
    val deleted: List<DeletedItemDto> = emptyList(),
)

data class ReminderSyncDto(
    val id: UUID,
    val title: String,
    @field:Valid val schedule: ScheduleDto,
    val enabled: Boolean,
    val updatedAt: Instant,
) {
    fun toChange() = ReminderChange(
        id = id, title = title, schedule = schedule.toDomain(),
        enabled = enabled, updatedAt = updatedAt,
    )
}

data class StudyItemSyncDto(
    val id: UUID,
    val kind: StudyItemKind,
    val prompt: String,
    val answer: String,
    val enabled: Boolean,
    val updatedAt: Instant,
) {
    fun toChange() = StudyItemChange(
        id = id, kind = kind, prompt = prompt,
        answer = answer, enabled = enabled, updatedAt = updatedAt,
    )
}

data class NotificationSettingSyncDto(
    val frequency: Frequency,
    val enabled: Boolean,
    val updatedAt: Instant,
) {
    fun toChange() = NotificationSettingChange(
        frequency = frequency, enabled = enabled, updatedAt = updatedAt,
    )
}

data class DeletedItemDto(
    val id: UUID,
    val deletedAt: Instant,
)

// --- POST レスポンス ---

data class SyncPushResponse(
    val reminders: SyncEntityResponse,
    val studyItems: SyncEntityResponse,
    val studyNotificationSetting: NotificationSettingResponse?,
    val syncedAt: Instant,
) {
    companion object {
        fun from(result: SyncResult) = SyncPushResponse(
            reminders = SyncEntityResponse(
                upserted = result.reminders.upserted.map(ReminderResponse::from),
                deleted = result.reminders.deleted.map { it.toString() },
            ),
            studyItems = SyncEntityResponse(
                upserted = result.studyItems.upserted.map(StudyItemResponse::from),
                deleted = result.studyItems.deleted.map { it.toString() },
            ),
            studyNotificationSetting = result.studyNotificationSetting?.let(NotificationSettingResponse::from),
            syncedAt = result.syncedAt,
        )
    }
}

data class SyncEntityResponse(
    val upserted: List<Any>,
    val deleted: List<String>,
)
