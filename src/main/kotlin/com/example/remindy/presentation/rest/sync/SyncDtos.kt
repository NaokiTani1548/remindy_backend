package com.example.remindy.presentation.rest.sync

import com.example.remindy.application.sync.SyncSnapshot
import com.example.remindy.presentation.rest.reminder.ReminderResponse
import com.example.remindy.presentation.rest.study.NotificationSettingResponse
import com.example.remindy.presentation.rest.study.StudyItemResponse
import java.time.OffsetDateTime

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
