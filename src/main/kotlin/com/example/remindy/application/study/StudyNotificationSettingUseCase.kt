package com.example.remindy.application.study

import com.example.remindy.application.study.command.UpdateNotificationSettingCommand
import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.study.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class StudyNotificationSettingUseCase(
    private val repository: StudyNotificationSettingRepository,
) {
    @Transactional
    fun get(userId: UserId): StudyNotificationSetting =
        repository.findByUserId(userId)
            ?: repository.save(StudyNotificationSetting.createDefault(userId))

    @Transactional
    fun update(command: UpdateNotificationSettingCommand): StudyNotificationSetting {
        val current = repository.findByUserId(command.userId)
            ?: StudyNotificationSetting.createDefault(command.userId)
        return repository.save(current.update(command.frequency, command.enabled))
    }
}
