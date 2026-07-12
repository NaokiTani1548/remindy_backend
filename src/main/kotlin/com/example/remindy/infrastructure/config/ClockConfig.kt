package com.example.remindy.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class ClockConfig {

    @Bean
    fun clock(): Clock =
        Clock.system(ZoneId.of("Asia/Tokyo"))
}