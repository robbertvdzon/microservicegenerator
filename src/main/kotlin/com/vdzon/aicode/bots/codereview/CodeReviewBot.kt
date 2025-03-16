package com.vdzon.aicode.bots.codereview

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.git.GithubService
import com.vdzon.aicode.aiengine.AiEngineFactory
import com.vdzon.aicode.aiengine.util.JsonSchemaHelper
import com.vdzon.aicode.bots.AIBot

class CodeReviewBot(
): AIBot {
    override fun getName(): String = "code_review"
    override fun getDescription(): String = "Code review a story"
    override fun getHelp(): String = "code_review githubrepo mainbranch featurebranch story engine model"
    override fun run(args: Array<String>): String{
        val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
        val mainbranch = args.getOrNull(2) ?: throw RuntimeException("Invalid main branch")
        val featurebranch = args.getOrNull(3) ?: throw RuntimeException("feature branch")
        val story = args.getOrNull(4) ?: throw RuntimeException("Invalid story")
        val engine = args.getOrNull(5) ?: throw RuntimeException("Invalid engine")
        val model = args.getOrNull(6) ?: throw RuntimeException("Invalid model")

        val tokenGenerator = Tokens()
        val aiEngine= AiEngineFactory.getAiEngine(engine, model)
        val githubService =  GithubService()

        println("\nStart code review..")
        val storyToImplement = githubService.getTicket(repo, story)
        val mainCode = githubService.getSerializedRepo(mainbranch)
        val branch = githubService.getSerializedRepo(featurebranch)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = Request(mainCode!!, branch!!, storyToImplement)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val jsonSchema: Map<String, Any> = JsonSchemaHelper.generateJsonSchemaAsMap(AiResponse::class.java)
        val jsonResponse = aiEngine.chat(jsonSchema, tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson))
        val aiResponse: AiResponse = jacksonObjectMapper().readValue(jsonResponse,object : TypeReference<AiResponse>() {})
        val endTime = System.currentTimeMillis()

        val output = buildString {
            append("Ai finished in: ${endTime - startTime} ms\n\n")
            append("Review response for each file:\n")

            aiResponse.filesWithComments?.forEach {
                append("File: ${it.sourceFilename?.filename}\n")
                append("${it.codeReviewComments}\n\n")
            }

            append("General code review comments:\n")
            append(aiResponse.generalCodeReviewComments)
        }
        println(output)
        return output
    }


}