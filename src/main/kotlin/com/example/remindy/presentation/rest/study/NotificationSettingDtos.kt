package com.example.remindy.presentation.rest.study

import com.example.remindy.domain.study.Frequency
import com.example.remindy.domain.study.StudyNotificationSetting
import jakarta.validation.constraints.NotNull
import java.time.LocalTime

data class ActiveHoursDto(val start: LocalTime, val end: LocalTime)

data class UpdateNotificationSettingRequest(
    @field:NotNull val frequency: Frequency,
    val enabled: Boolean,
)

data class NotificationSettingResponse(
    val frequency: Frequency,
    val activeHours: ActiveHoursDto,
    val enabled: Boolean,
) {
    companion object {
        fun from(setting: StudyNotificationSetting) = NotificationSettingResponse(
            frequency = setting.frequency,
            activeHours = ActiveHoursDto(
                StudyNotificationSetting.ACTIVE_HOURS_START,
                StudyNotificationSetting.ACTIVE_HOURS_END,
            ),
            enabled = setting.enabled,
        )
    }
}
