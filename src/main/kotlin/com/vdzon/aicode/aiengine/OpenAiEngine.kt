package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.model.request.AIRequest
import com.vdzon.aicode.model.response.AiResponse
import com.vdzon.aicode.model.request.Message
import com.vdzon.aicode.model.openai.OpenAIResponse
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class OpenAiEngine(val model: String) : AIEngine {
    override fun chat(jsonSchema: Map<String, Any>, systemPrompt: String, userPrompt: String): String {
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
        println("OpenAI: prompt tokens: ${openAiResponse.usage.prompt_tokens} completion tokens: ${openAiResponse.usage.completion_tokens} total tokens: ${openAiResponse.usage.total_tokens} model: ${openAiResponse.model}")

        val json = openAiResponse?.choices?.firstOrNull()?.message?.content ?: ""
        return json


    }

}