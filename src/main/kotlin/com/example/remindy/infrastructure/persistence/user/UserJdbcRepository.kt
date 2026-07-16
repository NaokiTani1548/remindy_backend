package com.example.remindy.infrastructure.persistence.user

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface UserJdbcRepository : CrudRepository<UserRecord, UUID> {
    fun findByEmailAddress(emailAddress: String): UserRecord?
    fun existsByEmailAddress(emailAddress: String): Boolean
}
