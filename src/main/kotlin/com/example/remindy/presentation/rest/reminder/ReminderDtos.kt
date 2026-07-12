package com.example.remindy.presentation.rest.reminder

import com.example.remindy.domain.reminder.Reminder
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

/** 作成リクエスト。入口の粗い検証(@NotBlank)はここ。業務不変条件(100字以内等)はドメインVOが最終防衛線。 */
data class CreateReminderRequest(
    @field:NotBlank val title: String,
    @field:Valid val schedule: ScheduleDto,
)

/** PUT: 内容(タイトル・スケジュール)の置換。作成と同形。 */
data class UpdateReminderRequest(
    @field:NotBlank val title: String,
    @field:Valid val schedule: ScheduleDto,
)

/** PATCH: 状態(有効/無効)のみの部分更新。 */
data class ToggleEnabledRequest(
    val enabled: Boolean,
)

/** 応答表現。ドメインを直接JSON化せず、必ずDTOに写して外部契約を安定させる。 */
data class ReminderResponse(
    val id: String,
    val title: String,
    val schedule: ScheduleDto,
    val enabled: Boolean,
) {
    companion object {
        fun from(reminder: Reminder) = ReminderResponse(
            id = reminder.id!!.value.toString(),
            title = reminder.title.value,
            schedule = ScheduleDto.fromDomain(reminder.schedule),
            enabled = reminder.enabled,
        )
    }
}

/** 一覧レスポンス。API仕様の {"items":[...]} に対応。 */
data class ReminderListResponse(
    val items: List<ReminderResponse>,
)