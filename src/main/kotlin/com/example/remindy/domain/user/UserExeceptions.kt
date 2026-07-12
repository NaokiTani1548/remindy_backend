package com.example.remindy.domain.user

class EmailAlreadyUsedException(email: EmailAddress) :
    RuntimeException("Email already used: $email")

class InvalidCredentialsException :
    RuntimeException("Invalid email or password")

class UserNotFoundException(id: String) :
    RuntimeException("User not found: $id")
