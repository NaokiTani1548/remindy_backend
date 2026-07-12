package com.example.remindy.presentation.rest.auth

import com.example.remindy.application.user.IssuedToken
import com.example.remindy.domain.user.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank @field:Email @field:Size(max = 254) val emailAddress: String,
    @field:NotBlank @field:Size(min = 8, max = 100) val password: String,
)

data class LoginRequest(
    @field:NotBlank @field:Email val emailAddress: String,
    @field:NotBlank val password: String,
)

data class RegisterResponse(val id: String, val emailAddress: String) {
    companion object {
        fun from(user: User) = RegisterResponse(user.id!!.value.toString(), user.email.value)
    }
}

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,
) {
    companion object {
        fun from(token: IssuedToken) =
            LoginResponse(token.value, "Bearer", token.expiresInSeconds)
    }
}

data class MeResponse(val id: String, val emailAddress: String) {
    companion object {
        fun from(user: User) = MeResponse(user.id!!.value.toString(), user.email.value)
    }
}
