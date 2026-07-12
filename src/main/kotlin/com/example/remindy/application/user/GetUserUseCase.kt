package com.example.remindy.application.user

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.user.User
import com.example.remindy.domain.user.UserNotFoundException
import com.example.remindy.domain.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetUserUseCase(
    private val userRepository: UserRepository,
) {
    @Transactional(readOnly = true)
    fun getById(userId: UserId): User =
        userRepository.findById(userId) ?: throw UserNotFoundException(userId.value.toString())
}
