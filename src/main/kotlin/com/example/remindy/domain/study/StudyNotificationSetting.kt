package com.example.remindy.domain.study

import com.example.remindy.domain.shared.UserId
import java.time.LocalTime

class StudyNotificationSetting private constructor(
    val userId: UserId,
    val frequency: Frequency,
    val enabled: Boolean,
) {
    companion object {
        val ACTIVE_HOURS_START: LocalTime = LocalTime.of(9, 0)
        val ACTIVE_HOURS_END: LocalTime = LocalTime.of(21, 0)

        fun createDefault(userId: UserId): StudyNotificationSetting =
            StudyNotificationSetting(userId, Frequency.THREE_TIMES, enabled = true)

        fun reconstitute(userId: UserId, frequency: Frequency, enabled: Boolean): StudyNotificationSetting =
            StudyNotificationSetting(userId, frequency, enabled)
    }

    fun update(frequency: Frequency, enabled: Boolean): StudyNotificationSetting =
        StudyNotificationSetting(userId, frequency, enabled)
}
