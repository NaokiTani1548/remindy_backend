
package com.example.remindy.application.user

import com.example.remindy.domain.shared.UserId

data class IssuedToken(val value: String, val expiresInSeconds: Long)

interface TokenIssuer {
    fun issue(userId: UserId): IssuedToken
}
