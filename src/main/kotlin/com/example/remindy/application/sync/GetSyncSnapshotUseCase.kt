package com.example.remindy.application.sync

import com.example.remindy.domain.reminder.Reminder
import com.example.remindy.domain.reminder.ReminderRepository
import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.StudyItem
import com.example.remindy.domain.study.StudyItemRepository
import com.example.remindy.domain.study.StudyNotificationSetting
import com.example.remindy.application.study.StudyNotificationSettingUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.OffsetDateTime

data class SyncSnapshot(
    val reminders: List<Reminder>,
    val studyItems: List<StudyItem>,
    val studyNotificationSetting: StudyNotificationSetting,
    val serverTime: OffsetDateTime,
)

@Service
class GetSyncSnapshotUseCase(
    private val reminderRepository: ReminderRepository,
    private val studyItemRepository: StudyItemRepository,
    private val settingUseCase: StudyNotificationSettingUseCase,
    private val clock: Clock,
) {
    @Transactional
    fun handle(userId: UserId): SyncSnapshot =
        SyncSnapshot(
            reminders = reminderRepository.findByUserId(userId),
            studyItems = studyItemRepository.findByUserId(userId),
            studyNotificationSetting = settingUseCase.get(userId),
            serverTime = OffsetDateTime.now(clock),
        )
}
