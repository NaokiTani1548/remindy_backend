package com.example.remindy

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<RemindyApplication>().with(TestcontainersConfiguration::class).run(*args)
}
