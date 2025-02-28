package com.vdzon.aicode


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


class Application

fun main(args: Array<String>) {
    val githubService: GithubService = GithubService()
    val codeGeneratorService = CodeGeneratorService(githubService)
    codeGeneratorService.generateCode()

}

