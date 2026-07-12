package com.example.remindy.application.reminder.command

import com.example.remindy.domain.reminder.ReminderId
import com.example.remindy.domain.shared.UserId

/** 削除コマンド。DELETE /reminders/{id} に対応。 */
data class DeleteReminderCommand(
    val userId: UserId,
    val id: ReminderId,
)