package com.example.remindy.application.user

interface PasswordHasher {
    fun hash(rawPassword: String): String
    fun matches(rawPassword: String, hashed: String): Boolean
}
