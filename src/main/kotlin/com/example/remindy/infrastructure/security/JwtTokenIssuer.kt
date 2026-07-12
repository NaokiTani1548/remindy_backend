package com.example.remindy.infrastructure.security

import com.example.remindy.application.user.IssuedToken
import com.example.remindy.application.user.TokenIssuer
import com.example.remindy.domain.shared.UserId
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class JwtTokenIssuer(
    private val jwtEncoder: JwtEncoder,
    private val jwtProperties: JwtProperties,
    private val clock: Clock,
) : TokenIssuer {
    override fun issue(userId: UserId): IssuedToken {
        val now: Instant = Instant.now(clock)
        val expiresAt = now.plus(jwtProperties.expiresInSeconds, ChronoUnit.SECONDS)
        val claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.issuer)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(userId.value.toString())
            .build()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
        return IssuedToken(value = token, expiresInSeconds = jwtProperties.expiresInSeconds)
    }
}
