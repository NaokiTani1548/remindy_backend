package com.example.remindy.presentation.rest.auth

import com.example.remindy.application.user.*
import com.example.remindy.application.user.command.LoginCommand
import com.example.remindy.application.user.command.RegisterUserCommand
import com.example.remindy.presentation.security.AuthenticatedUserProvider
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerUser: RegisterUserUseCase,
    private val login: LoginUseCase,
    private val getUser: GetUserUseCase,
    private val currentUser: AuthenticatedUserProvider,
) {
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest): RegisterResponse {
        val user = registerUser.handle(RegisterUserCommand(request.emailAddress, request.password))
        return RegisterResponse.from(user)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val token = login.handle(LoginCommand(request.emailAddress, request.password))
        return LoginResponse.from(token)
    }

    @GetMapping("/me")
    fun me(): MeResponse = MeResponse.from(getUser.getById(currentUser.currentUserId()))
}
