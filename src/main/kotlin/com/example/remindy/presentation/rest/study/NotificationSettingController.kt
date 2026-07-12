package com.example.remindy.presentation.rest.study

import com.example.remindy.application.study.StudyNotificationSettingUseCase
import com.example.remindy.application.study.command.UpdateNotificationSettingCommand
import com.example.remindy.presentation.security.AuthenticatedUserProvider
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/study/notification-setting")
class NotificationSettingController(
    private val useCase: StudyNotificationSettingUseCase,
    private val currentUser: AuthenticatedUserProvider,
) {
    @GetMapping
    fun get(): NotificationSettingResponse =
        NotificationSettingResponse.from(useCase.get(currentUser.currentUserId()))

    @PutMapping
    fun update(@Valid @RequestBody req: UpdateNotificationSettingRequest): NotificationSettingResponse =
        NotificationSettingResponse.from(
            useCase.update(
                UpdateNotificationSettingCommand(
                    currentUser.currentUserId(), req.frequency, req.enabled
                )
            )
        )
}
