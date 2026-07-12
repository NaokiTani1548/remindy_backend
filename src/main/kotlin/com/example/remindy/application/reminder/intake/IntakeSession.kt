package com.example.remindy.application.reminder.intake

import com.example.remindy.domain.shared.UserId
import java.time.Instant
import java.util.UUID

data class IntakeSession(
    val id: UUID,
    val userId: UserId,
    val draft: ReminderDraft,
    val createdAt: Instant,
) {
    fun withDraft(newDraft: ReminderDraft): IntakeSession = copy(draft = newDraft)
}
