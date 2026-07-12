package com.example.remindy.application.reminder.intake

import com.example.remindy.application.reminder.intake.command.ContinueIntakeCommand
import com.example.remindy.domain.shared.UserId
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.OffsetDateTime
import java.util.UUID



@Service
class ContinueIntakeUseCase(
    private val parser: ReminderParser,
    private val sessionStore: IntakeSessionStore,
    private val clock: Clock,
) {
    fun handle(command: ContinueIntakeCommand): IntakeView {
        val session = sessionStore.find(command.sessionId)
            ?.takeIf { it.userId == command.userId }
            ?: throw IntakeSessionNotFoundException(command.sessionId)

        val referenceTime = OffsetDateTime.now(clock)
        val parsed = parser.parse(command.text, referenceTime)
        val mergedDraft = session.draft.mergedWith(parsed)
        val updated = sessionStore.save(session.withDraft(mergedDraft))

        return IntakeView(
            sessionId = updated.id,
            status = mergedDraft.status(),
            assistantMessage = IntakeMessages.forDraft(mergedDraft),
            draft = mergedDraft,
            missingElements = mergedDraft.missingElements(),
        )
    }
}
