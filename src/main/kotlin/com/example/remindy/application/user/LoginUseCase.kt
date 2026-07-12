package com.example.remindy.application.user

import com.example.remindy.application.user.command.LoginCommand
import com.example.remindy.domain.user.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val tokenIssuer: TokenIssuer,
) {
    @Transactional(readOnly = true)
    fun handle(command: LoginCommand): IssuedToken {
        val email = EmailAddress.of(command.email)
        val user = userRepository.findByEmail(email) ?: throw InvalidCredentialsException()
        if (!passwordHasher.matches(command.rawPassword, user.passwordHash.value)) {
            throw InvalidCredentialsException()
        }
        return tokenIssuer.issue(user.id!!)
    }
}
