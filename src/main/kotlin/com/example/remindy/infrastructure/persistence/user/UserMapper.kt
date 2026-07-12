package com.example.remindy.infrastructure.persistence.user

import com.example.remindy.domain.shared.UserId
import com.example.remindy.domain.user.EmailAddress
import com.example.remindy.domain.user.PasswordHash
import com.example.remindy.domain.user.User
import java.time.Instant

object UserMapper {
    fun toNewRecord(user: User, now: Instant): UserRecord =
        UserRecord(
            id = null,
            emailAddress = user.email.value,
            passwordHash = user.passwordHash.value,
            createdAt = now,
            updatedAt = now,
        )

    fun toExistingRecord(user: User, createdAt: Instant, updatedAt: Instant): UserRecord =
        UserRecord(
            id = user.id!!.value,
            emailAddress = user.email.value,
            passwordHash = user.passwordHash.value,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    fun toDomain(record: UserRecord): User =
        User.reconstitute(
            id = UserId(record.id!!),
            email = EmailAddress.of(record.emailAddress),
            passwordHash = PasswordHash.of(record.passwordHash),
        )
}
