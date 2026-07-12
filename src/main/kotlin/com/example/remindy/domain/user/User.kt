package com.example.remindy.domain.user

import com.example.remindy.domain.shared.UserId

class User private constructor(
    val id: UserId?,
    val email: EmailAddress,
    val passwordHash: PasswordHash,
) {
    val isPersisted: Boolean get() = id != null

    companion object {
        fun create(email: EmailAddress, passwordHash: PasswordHash): User =
            User(id = null, email = email, passwordHash = passwordHash)

        fun reconstitute(id: UserId, email: EmailAddress, passwordHash: PasswordHash): User =
            User(id, email, passwordHash)
    }
}
