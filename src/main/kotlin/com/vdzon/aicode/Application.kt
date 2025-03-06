package com.vdzon.aicode

import com.vdzon.aicode.bots.checkstory.CheckStoryService
import com.vdzon.aicode.bots.codegenerator.CodeGeneratorService


class Application

fun main(args: Array<String>) {
    val action = args.getOrNull(0)
    when (action) {
        null -> noAction()
        "check_story" -> checkStory(args)
        "implement_story" -> generateCode(args)
        "code_review" -> notImplemented()
        "explain_branch" -> notImplemented()
        "validate_specs" -> notImplemented()
        "generate_specs" -> notImplemented()
        else -> invalidAction()
    }
}

private fun invalidAction() {
    println("Invalid action")
}

private fun generateCode(args: Array<String>) {
    val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
    val mainbranch = args.getOrNull(2) ?: throw RuntimeException("Invalid main branch")
    val featurebranch = args.getOrNull(3) ?: throw RuntimeException("feature branch")
    val story = args.getOrNull(4) ?: throw RuntimeException("Invalid story")
    val engine = args.getOrNull(5) ?: throw RuntimeException("Invalid engine")
    val model = args.getOrNull(6) ?: throw RuntimeException("Invalid model")
    println("Generating code:")
    println("repo: $repo")
    println("mainbranch: $mainbranch")
    println("featurebranch: $featurebranch")
    println("story: $story")
    println("engine: $engine")
    println("model: $model")


    val githubService: GithubService = GithubService()
    val codeGeneratorService =
        CodeGeneratorService(githubService, repo, mainbranch, featurebranch, story, engine, model)
    codeGeneratorService.generateCode()
}

private fun checkStory(args: Array<String>) {
    val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
    val mainbranch = args.getOrNull(2) ?: throw RuntimeException("Invalid main branch")
    val story = args.getOrNull(3) ?: throw RuntimeException("Invalid story")
    val engine = args.getOrNull(4) ?: throw RuntimeException("Invalid engine")
    val model = args.getOrNull(5) ?: throw RuntimeException("Invalid model")
    println("Generating code:")
    println("repo: $repo")
    println("mainbranch: $mainbranch")
    println("story: $story")
    println("engine: $engine")
    println("model: $model")


    val githubService = GithubService()
    val bot = CheckStoryService(githubService, repo, mainbranch, story, engine, model)
    bot.generateCode()
}


private fun noAction() {
    println("No action")
    help()
}

private fun notImplemented() {
    println("Not implemented")
    help()
}

private fun help() {
    println("Please use one of the following actions:")
    println("./ghostwriter check_story githubrepo mainbranch story engine model # Check if a story is clear enough to implement")
    println("./ghostwriter implement_story githubrepo mainbranch featurebranch story engine model  # Implement a story")
    println("./ghostwriter code_review githubrepo mainbranch featurebranch story engine model  # Review the code of a story")
    println("./ghostwriter explain_branch githubrepo mainbranch featurebranch story engine model  # Explain the changes in a branch")
    println("./ghostwriter validate_specs githubrepo branch engine model  # Validate if the specs of a branch are complete and not in conflict with each other")
    println("./ghostwriter generate_specs githubrepo branch engine model  # Generate specs of a branch")
    println("\n engine can be one of the following: OPEN_AI, OLLAMA")
    println("\n model can be one of the following: gpt-4.5-preview, gpt-4o, gpt-3.5-turbo, qwen2.5-coder:32b, qwen2.5-coder:14b, qwen2.5-coder:7b")
}

