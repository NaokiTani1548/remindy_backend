package com.example.remindy.application.reminder.command

import com.example.remindy.domain.reminder.ReminderId
import com.example.remindy.domain.shared.UserId

/** 有効/無効の切替コマンド。PATCH /reminders/{id} に対応。 */
data class SetReminderEnabledCommand(
    val userId: UserId,
    val id: ReminderId,
    val enabled: Boolean,
)