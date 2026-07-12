package com.example.remindy.domain.user

import com.example.remindy.domain.shared.UserId

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByEmail(email: EmailAddress): User?
    fun existsByEmail(email: EmailAddress): Boolean
}
