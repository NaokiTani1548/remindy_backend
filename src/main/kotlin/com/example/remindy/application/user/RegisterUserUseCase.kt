package com.example.remindy.application.user

import com.example.remindy.application.user.command.RegisterUserCommand
import com.example.remindy.domain.study.StudyNotificationSetting
import com.example.remindy.domain.study.StudyNotificationSettingRepository
import com.example.remindy.domain.user.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val studyNotificationSettingRepository: StudyNotificationSettingRepository,
) {
    @Transactional
    fun handle(command: RegisterUserCommand): User {
        val email = EmailAddress.of(command.email)
        if (userRepository.existsByEmail(email)) throw EmailAlreadyUsedException(email)

        val hash = PasswordHash.of(passwordHasher.hash(command.rawPassword))
        val user = userRepository.save(User.create(email, hash))

        studyNotificationSettingRepository.save(StudyNotificationSetting.createDefault(user.id!!))
        return user
    }
}
