package com.example.remindy

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class RemindyApplicationTests {

    @Test
    fun contextLoads() {
    }

}
