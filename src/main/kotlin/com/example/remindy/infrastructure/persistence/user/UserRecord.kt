package com.example.remindy.infrastructure.persistence.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("users")
data class UserRecord(
    @Id val id: UUID?,
    val emailAddress: String,
    val passwordHash: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
