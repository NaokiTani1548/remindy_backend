package com.example.remindy.infrastructure.llm

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "remindy.intake")
data class IntakeProperties(
    /** インテークセッションの失効時間(秒)。既定は30分。 */
    val sessionTtlSeconds: Long = 1_800,
)
