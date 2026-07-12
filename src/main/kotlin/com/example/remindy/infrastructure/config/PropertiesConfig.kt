package com.example.remindy.infrastructure.config

import com.example.remindy.infrastructure.llm.LlmProperties
import com.example.remindy.infrastructure.llm.IntakeProperties
import com.example.remindy.infrastructure.security.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, LlmProperties::class, IntakeProperties::class)
class PropertiesConfig
