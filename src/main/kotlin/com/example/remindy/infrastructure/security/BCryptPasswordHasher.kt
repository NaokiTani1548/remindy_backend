package com.example.remindy.infrastructure.security

import com.example.remindy.application.user.PasswordHasher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasher(
    private val passwordEncoder: PasswordEncoder,
) : PasswordHasher {
    override fun hash(rawPassword: String): String = passwordEncoder.encode(rawPassword)!!
    override fun matches(rawPassword: String, hashed: String): Boolean =
        passwordEncoder.matches(rawPassword, hashed)
}
