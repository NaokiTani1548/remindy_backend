package com.example.remindy.application.reminder.command

import com.example.remindy.domain.reminder.Schedule
import com.example.remindy.domain.shared.UserId

/**
 * リマインダー作成の入力(コマンド)。
 * 既に型付き・検証済みの値だけを受ける。文字列やHTTPの都合はここに現れない。
 * title を String でなく…と迷うが、VO化(ReminderTitle)は presentation の責務なので
 * ここでは String で受け、ユースケース内で ReminderTitle.of() を通す構成にする(下記参照)。
 */
data class CreateReminderCommand(
    val userId: UserId,
    val title: String,
    val schedule: Schedule,
)