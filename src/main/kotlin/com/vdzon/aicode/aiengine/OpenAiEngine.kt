package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.model.AIRequest
import com.vdzon.aicode.model.AiResponse
import com.vdzon.aicode.model.Message
import com.vdzon.aicode.model.openai.OpenAIResponse
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class OpenAiEngine(val model: String) : AIEngine {
    override fun chat(systemPrompt: String, userPrompt: String): AiResponse {
        val jsonSchema = generateJsonSchemaAsMap(AiResponse::class.java)
        val request = AIRequest(
            model = model,
            messages = listOf(
                Message("system", systemPrompt),
                Message("user", userPrompt)
            ),
            format = jsonSchema
        )
        val apiKey = System.getenv("OPENAI_API_KEY")
        val url = URL("https://api.openai.com/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer $apiKey")

        connection.doOutput = true
        val requesJson = jacksonObjectMapper().writeValueAsString(request)
        connection.outputStream.use { it.write(requesJson.toByteArray()) }
        val responseJson = connection.inputStream.bufferedReader().use(BufferedReader::readText)
        val openAiResponse = jacksonObjectMapper().readValue(responseJson, object : TypeReference<OpenAIResponse>() {})

        println("\n\n\n")
        println("openAiResponse: \n$responseJson")
        println("\n\n\n")
        val aiResponse = jacksonObjectMapper().readValue(
            openAiResponse?.choices?.firstOrNull()?.message?.content ?: "",
            object : TypeReference<AiResponse>() {})
        return aiResponse


    }

}