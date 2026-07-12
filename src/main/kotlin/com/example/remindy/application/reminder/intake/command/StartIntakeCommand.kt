package com.example.remindy.application.reminder.intake.command

import com.example.remindy.domain.shared.UserId
import java.time.OffsetDateTime

data class StartIntakeCommand(
    val userId: UserId,
    val text: String,
    val clientTime: OffsetDateTime?,
)