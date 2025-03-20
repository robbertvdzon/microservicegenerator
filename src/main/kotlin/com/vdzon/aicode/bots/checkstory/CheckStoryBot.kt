package com.vdzon.aicode.bots.checkstory

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.git.GithubService
import com.vdzon.aicode.aiengine.AiEngineFactory
import com.vdzon.aicode.aiengine.util.JsonSchemaHelper
import com.vdzon.aicode.bots.AIBot
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class CheckStoryBot(val aiEngineFactory: AiEngineFactory): AIBot {

    override fun getName(): String = "check_story"
    override fun getDescription(): String = "Check the story"
    override fun getHelp(): String = "check_story githubrepo mainbranch story engine model"
    override fun run(args: Array<String>): String{
        val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
        val sourceFolder = args.getOrNull(2) ?: throw RuntimeException("Invalid sourceFolder")
        val mainbranch = args.getOrNull(3) ?: throw RuntimeException("Invalid main branch")
        val featurebranch = args.getOrNull(4) ?: throw RuntimeException("feature branch")
        val story = args.getOrNull(5) ?: throw RuntimeException("Invalid story")
        val engine = args.getOrNull(6) ?: throw RuntimeException("Invalid engine")
        val model = args.getOrNull(7) ?: throw RuntimeException("Invalid model")
        val question = args.getOrNull(8) ?: throw RuntimeException("Invalid question")

        val tokenGenerator = Tokens()
        val aiEngine= aiEngineFactory.getAiEngine(engine)
        val githubService =  GithubService()

        println("\nStart generating code..")
        val storyToImplement = githubService.getTicket(repo, story)
        println("Story to implement: ${storyToImplement.title}")
        println("${storyToImplement.body}")
        val mainCode = githubService.getSerializedRepo(repo, mainbranch, sourceFolder)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = CheckStoryRequest(mainCode!!, storyToImplement)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val jsonSchema: Map<String, Any> = JsonSchemaHelper.generateJsonSchemaAsMap(CheckStoryAiResponse::class.java)
        val jsonResponse = aiEngine.chat(jsonSchema, tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson), model)
        val aiResponse: CheckStoryAiResponse = jacksonObjectMapper().readValue(jsonResponse,object : TypeReference<CheckStoryAiResponse>() {})

        val endTime = System.currentTimeMillis()

        val output = buildString {
            val timeInSeconds = (endTime - startTime)/1000
            append("Ai finished in: $timeInSeconds sec, using $engine : $model\n\n")
            append("\nComments about story: ")
            append(aiResponse.commentsAboutStory)
            append("\nNew suggested story name: ")
            append(aiResponse.newSuggestedStoryName)
            append("\nNew suggested body: ")
            append(aiResponse.newSuggestedStoryBody)
        }
        println(output)
        return output

    }


    fun generateCode() {

    }


}