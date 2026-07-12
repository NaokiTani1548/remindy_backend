package com.example.remindy.presentation.rest.reminder

import com.example.remindy.application.reminder.*
import com.example.remindy.application.reminder.command.CreateReminderCommand
import com.example.remindy.application.reminder.command.DeleteReminderCommand
import com.example.remindy.application.reminder.command.SetReminderEnabledCommand
import com.example.remindy.application.reminder.command.UpdateReminderCommand
import com.example.remindy.domain.reminder.ReminderId
import com.example.remindy.presentation.security.AuthenticatedUserProvider
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@RestController
@RequestMapping("/api/v1/reminders")
class ReminderController(
    private val createReminder: CreateReminderUseCase,
    private val updateReminder: UpdateReminderUseCase,
    private val queryReminder: ReminderQueryUseCase,
    private val currentUser: AuthenticatedUserProvider,
) {
    @PostMapping
    fun create(
        @Valid @RequestBody req: CreateReminderRequest,
        uriBuilder: UriComponentsBuilder,
    ): ResponseEntity<ReminderResponse> {
        val reminder = createReminder.handle(
            CreateReminderCommand(currentUser.currentUserId(), req.title, req.schedule.toDomain())
        )
        val location = uriBuilder.path("/api/v1/reminders/{id}")
            .buildAndExpand(reminder.id!!.value).toUri()
        return ResponseEntity.created(location).body(ReminderResponse.from(reminder))
    }

    @GetMapping
    fun list(): ReminderListResponse =
        ReminderListResponse(queryReminder.list(currentUser.currentUserId()).map(ReminderResponse::from))

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): ReminderResponse =
        ReminderResponse.from(queryReminder.get(currentUser.currentUserId(), ReminderId(id)))

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody req: UpdateReminderRequest): ReminderResponse =
        ReminderResponse.from(
            updateReminder.changeContent(
                UpdateReminderCommand(
                    currentUser.currentUserId(), ReminderId(id), req.title, req.schedule.toDomain()
                )
            )
        )

    @PatchMapping("/{id}")
    fun toggle(@PathVariable id: UUID, @RequestBody req: ToggleEnabledRequest): ReminderResponse =
        ReminderResponse.from(updateReminder.setEnabled(
            SetReminderEnabledCommand(
                currentUser.currentUserId(), ReminderId(id), req.enabled
            )
            )
        )

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        updateReminder.delete(
            DeleteReminderCommand(
                currentUser.currentUserId(), ReminderId(id)
            )
        )
        return ResponseEntity.noContent().build()
    }
}
