package com.example.remindy.application.study.command

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.Frequency

/**
 * 学習通知設定の更新コマンド。PUT /study/notification-setting に対応。
 * 通知時間帯(9-21時)は固定定数のためコマンドに含めない
 * (API仕様: activeHours は送られても無視)。
 */
data class UpdateNotificationSettingCommand(
    val userId: UserId,
    val frequency: Frequency,
    val enabled: Boolean,
)