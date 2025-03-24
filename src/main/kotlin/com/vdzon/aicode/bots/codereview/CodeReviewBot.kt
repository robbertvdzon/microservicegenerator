package com.vdzon.aicode.bots.codereview

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.aiengine.AiEngineFactory
import com.vdzon.aicode.aiengine.util.JsonSchemaHelper
import com.vdzon.aicode.bots.AIBot
import com.vdzon.aicode.git.GithubService
import org.springframework.stereotype.Service

@Service
class CodeReviewBot(
    val aiEngineFactory: AiEngineFactory
) : AIBot {
    override fun getName(): String = "code_review"
    override fun getDescription(): String = "Code review a story"
    override fun getHelp(): String = "code_review githubrepo mainbranch featurebranch story engine model"
    override fun run(args: Array<String>): String {
        val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
        val sourceFolder = args.getOrNull(2) ?: throw RuntimeException("Invalid sourceFolder")
        val mainbranch = args.getOrNull(3) ?: throw RuntimeException("Invalid main branch")
        val featurebranch = args.getOrNull(4) ?: throw RuntimeException("feature branch")
        val story = args.getOrNull(5) ?: throw RuntimeException("Invalid story")
        val engine = args.getOrNull(6) ?: throw RuntimeException("Invalid engine")
        val model = args.getOrNull(7) ?: throw RuntimeException("Invalid model")
        val question = args.getOrNull(8) ?: throw RuntimeException("Invalid question")

        val tokenGenerator = Tokens()
        val aiEngine = aiEngineFactory.getAiEngine(engine, repo)
        val githubService = GithubService()

        println("\nStart code review..")
        val storyToImplement = githubService.getTicket(repo, story)
        val mainCode = githubService.getSerializedRepo(repo, mainbranch, sourceFolder)
        val branch = githubService.getSerializedRepo(repo, featurebranch, sourceFolder)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = Request(mainCode!!, branch!!, storyToImplement)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val jsonSchema: Map<String, Any> = JsonSchemaHelper.generateJsonSchemaAsMap(AiResponse::class.java)
        val jsonResponse = aiEngine.chat(jsonSchema, tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson), model)
        val aiResponse: AiResponse = jacksonObjectMapper().readValue(jsonResponse, object : TypeReference<AiResponse>() {})
        val endTime = System.currentTimeMillis()

        val output = buildString {
            val timeInSeconds = (endTime - startTime) / 1000
            append("Ai finished in: $timeInSeconds sec, using $engine : $model\n\n")
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