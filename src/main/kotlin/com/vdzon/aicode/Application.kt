package com.vdzon.aicode


class Application

fun main(args: Array<String>) {
    val githubService: GithubService = GithubService()
    val codeGeneratorService = CodeGeneratorService(githubService)
    codeGeneratorService.generateCode()

}

