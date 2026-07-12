package com.example.remindy.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "remindy.jwt")
data class JwtProperties(
    /** HS256 用の共有秘密鍵。32バイト(256bit)以上必須。 */
    val secret: String,
    /** アクセストークンの有効期限(秒)。既定は14日。 */
    val expiresInSeconds: Long = 1_209_600,
    val issuer: String = "remindy",
)
