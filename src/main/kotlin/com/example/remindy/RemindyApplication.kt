package com.example.remindy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RemindyApplication

fun main(args: Array<String>) {
    runApplication<RemindyApplication>(*args)
}
