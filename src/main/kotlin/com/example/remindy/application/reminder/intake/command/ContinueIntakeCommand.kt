package com.example.remindy.application.reminder.intake.command

import com.example.remindy.domain.shared.UserId
import java.util.UUID

data class ContinueIntakeCommand(
    val userId: UserId,
    val sessionId: UUID,
    val text: String,
)