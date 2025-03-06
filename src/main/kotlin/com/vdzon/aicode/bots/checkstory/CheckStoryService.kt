package com.vdzon.aicode.bots.checkstory

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.GithubService
import com.vdzon.aicode.aiengine.AiEngineFactory
import com.vdzon.aicode.aiengine.util.JsonSchemaHelper
import com.vdzon.aicode.bots.AIBot
import com.vdzon.aicode.bots.checkstory.CheckStoryTokens

class CheckStoryService(): AIBot {

    override fun getName(): String = "check_story"
    override fun getDescription(): String = "Check the story"
    override fun getHelp(): String = "check_story githubrepo mainbranch story engine model"
    override fun run(args: Array<String>){
        val repo = args.getOrNull(1) ?: throw RuntimeException("Invalid repo")
        val mainbranch = args.getOrNull(2) ?: throw RuntimeException("Invalid main branch")
        val story = args.getOrNull(3) ?: throw RuntimeException("Invalid story")
        val engine = args.getOrNull(4) ?: throw RuntimeException("Invalid engine")
        val model = args.getOrNull(5) ?: throw RuntimeException("Invalid model")

        val tokenGenerator = CheckStoryTokens()
        val aiEngine= AiEngineFactory.getAiEngine(engine, model)
        val githubService =  GithubService()

        println("\nStart generating code..")
        val storyToImplement = githubService.getTicket(repo, story)
        println("Story to implement: ${storyToImplement.title}")
        println("${storyToImplement.body}")
        val mainCode = githubService.getSerializedRepo(mainbranch)
        println("calling AI model..")

        val startTime = System.currentTimeMillis()
        val requestModel = CheckStoryRequest(mainCode!!, storyToImplement)
        val requestJson = jacksonObjectMapper().writeValueAsString(requestModel)
        val jsonSchema: Map<String, Any> = JsonSchemaHelper.generateJsonSchemaAsMap(CheckStoryAiResponse::class.java)
        val jsonResponse = aiEngine.chat(jsonSchema, tokenGenerator.getSystemToken(), tokenGenerator.getUserToken(requestJson))
        val aiResponse: CheckStoryAiResponse = jacksonObjectMapper().readValue(jsonResponse,object : TypeReference<CheckStoryAiResponse>() {})

        val endTime = System.currentTimeMillis()

        println("Ai finished in: ${endTime - startTime} ms")

        println("\nComments about story: ")
        println(aiResponse.commentsAboutStory)
        println("\nNew suggested story name: ")
        println(aiResponse.newSuggestedStoryName)
        println("\nNew suggested body: ")
        println(aiResponse.newSuggestedStoryBody)
    }


    fun generateCode() {

    }


}