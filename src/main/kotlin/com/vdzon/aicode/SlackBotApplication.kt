package com.vdzon.aicode

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SlackBotApplication

fun main(args: Array<String>) {
    runApplication<SlackBotApplication>(*args)
}
