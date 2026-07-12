package com.example.remindy.infrastructure.llm

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "remindy.llm")
data class LlmProperties(
    val apiKey: String = "",
    val model: String = "gemini-2.0-flash",
    val baseUrl: String = "https://generativelanguage.googleapis.com",
)
