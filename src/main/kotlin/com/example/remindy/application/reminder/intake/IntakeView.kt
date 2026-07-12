package com.example.remindy.application.reminder.intake

import java.util.UUID

data class IntakeView(
    val sessionId: UUID,
    val status: IntakeStatus,
    val assistantMessage: String,
    val draft: ReminderDraft,
    val missingElements: List<MissingElement>,
)
