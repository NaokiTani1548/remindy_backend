package com.example.remindy.infrastructure.persistence.user

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.user.EmailAddress
import com.example.remindy.domain.user.User
import com.example.remindy.domain.user.UserRepository
import org.springframework.stereotype.Repository
import java.time.Clock
import java.time.Instant

@Repository
class UserRepositoryImpl(
    private val jdbc: UserJdbcRepository,
    private val clock: Clock,
) : UserRepository {

    override fun save(user: User): User {
        val now = Instant.now(clock)
        val record = if (user.id == null) {
            UserMapper.toNewRecord(user, now)
        } else {
            val createdAt = jdbc.findById(user.id!!.value).map { it.createdAt }.orElse(now)
            UserMapper.toExistingRecord(user, createdAt = createdAt, updatedAt = now)
        }
        return UserMapper.toDomain(jdbc.save(record))
    }

    override fun findById(id: UserId): User? =
        jdbc.findById(id.value).map(UserMapper::toDomain).orElse(null)

    override fun findByEmail(email: EmailAddress): User? =
        jdbc.findByEmailAddress(email.value)?.let(UserMapper::toDomain)

    override fun existsByEmail(email: EmailAddress): Boolean =
        jdbc.existsByEmailAddress(email.value)
}
