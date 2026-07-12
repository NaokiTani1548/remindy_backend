package com.example.remindy.presentation.rest.reminder

import com.example.remindy.application.reminder.intake.*
import com.example.remindy.application.reminder.intake.command.ContinueIntakeCommand
import com.example.remindy.application.reminder.intake.command.StartIntakeCommand
import com.example.remindy.presentation.security.AuthenticatedUserProvider
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reminders/intake")
class IntakeController(
    private val startIntake: StartIntakeUseCase,
    private val continueIntake: ContinueIntakeUseCase,
    private val currentUser: AuthenticatedUserProvider,
) {
    @PostMapping
    fun start(@Valid @RequestBody req: IntakeStartRequest): IntakeResponse =
        IntakeResponse.from(
            startIntake.handle(StartIntakeCommand(currentUser.currentUserId(), req.text, req.clientTime))
        )

    @PostMapping("/{sessionId}/messages")
    fun message(
        @PathVariable sessionId: UUID,
        @Valid @RequestBody req: IntakeMessageRequest,
    ): IntakeResponse =
        IntakeResponse.from(
            continueIntake.handle(ContinueIntakeCommand(currentUser.currentUserId(), sessionId, req.text))
        )
}
