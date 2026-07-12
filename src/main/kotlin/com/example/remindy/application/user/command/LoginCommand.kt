package com.example.remindy.application.user.command

data class LoginCommand(val email: String, val rawPassword: String)