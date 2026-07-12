package com.example.remindy.presentation.rest.reminder

import com.example.remindy.application.reminder.intake.IntakeStatus
import com.example.remindy.application.reminder.intake.IntakeView
import com.example.remindy.application.reminder.intake.MissingElement
import com.example.remindy.application.reminder.intake.ReminderDraft
import jakarta.validation.constraints.NotBlank
import java.time.OffsetDateTime

data class IntakeStartRequest(
    @field:NotBlank val text: String,
    val clientTime: OffsetDateTime?,
)

data class IntakeMessageRequest(
    @field:NotBlank val text: String,
)

data class DraftDto(val title: String?, val schedule: ScheduleDto?) {
    companion object {
        fun from(draft: ReminderDraft) =
            DraftDto(draft.title, draft.schedule?.let(ScheduleDto::fromDomain))
    }
}

data class IntakeResponse(
    val sessionId: String,
    val status: IntakeStatus,
    val assistantMessage: String,
    val draft: DraftDto,
    val missingElements: List<MissingElement>,
) {
    companion object {
        fun from(view: IntakeView) = IntakeResponse(
            sessionId = view.sessionId.toString(),
            status = view.status,
            assistantMessage = view.assistantMessage,
            draft = DraftDto.from(view.draft),
            missingElements = view.missingElements,
        )
    }
}
