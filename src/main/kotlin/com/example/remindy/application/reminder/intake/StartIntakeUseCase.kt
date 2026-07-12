package com.example.remindy.application.reminder.intake

import com.example.remindy.application.reminder.intake.command.StartIntakeCommand
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID



@Service
class StartIntakeUseCase(
    private val parser: ReminderParser,
    private val sessionStore: IntakeSessionStore,
    private val clock: Clock,
) {
    fun handle(command: StartIntakeCommand): IntakeView {
        val referenceTime = command.clientTime ?: OffsetDateTime.now(clock)
        val draft = parser.parse(command.text, referenceTime)
        val session = sessionStore.save(
            IntakeSession(
                id = UUID.randomUUID(),
                userId = command.userId,
                draft = draft,
                createdAt = Instant.now(clock),
            )
        )
        return IntakeView(
            sessionId = session.id,
            status = draft.status(),
            assistantMessage = IntakeMessages.forDraft(draft),
            draft = draft,
            missingElements = draft.missingElements(),
        )
    }
}
