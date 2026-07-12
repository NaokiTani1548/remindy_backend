package com.example.remindy.presentation.security

import com.example.remindy.domain.shared.UserId
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AuthenticatedUserProvider {
    fun currentUserId(): UserId {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("認証情報がありません")
        val jwt = authentication.principal as? Jwt
            ?: throw IllegalStateException("JWTプリンシパルではありません")
        return UserId(UUID.fromString(jwt.subject))
    }
}
