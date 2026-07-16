package com.example.remindy.infrastructure.llm

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "remindy.llm")
data class LlmProperties(
    val apiKey: String = "",
    val model: String = "",
    val baseUrl: String = "https://generativelanguage.googleapis.com",
)
