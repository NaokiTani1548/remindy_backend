package com.example.remindy.application.reminder.command

import com.example.remindy.domain.reminder.ReminderId
import com.example.remindy.domain.reminder.Schedule
import com.example.remindy.domain.shared.UserId

/**
 * リマインダーの内容(タイトル・スケジュール)を置換するコマンド。PUT /reminders/{id} に対応。
 * title は String のまま受け、ユースケース内で ReminderTitle.of() を通す
 * (不変条件の検証はドメインVOの責務)。
 */
data class UpdateReminderCommand(
    val userId: UserId,
    val id: ReminderId,
    val title: String,
    val schedule: Schedule,
)