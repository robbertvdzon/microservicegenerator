package com.vdzon.aicode.aiengine.openai

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.aiengine.AIEngine
import com.vdzon.aicode.commonmodel.AIRequest
import com.vdzon.aicode.commonmodel.Message
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

@Service
class OpenAiEngine() : AIEngine {
    override fun chat(jsonSchema: Map<String, Any>, systemPrompt: String, userPrompt: String, model: String): String {
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

        if (connection.responseCode>299){
            val error = connection.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "Geen foutmelding beschikbaar"
            throw RuntimeException("Error during call to OpenAI, status:${connection.responseCode}, error: $error")
        }

        val responseJson = connection.inputStream.bufferedReader().use(BufferedReader::readText)
        val openAiResponse = jacksonObjectMapper().readValue(responseJson, object : TypeReference<OpenAIResponse>() {})
        println("OpenAI: prompt tokens: ${openAiResponse.usage.prompt_tokens} completion tokens: ${openAiResponse.usage.completion_tokens} total tokens: ${openAiResponse.usage.total_tokens} model: ${openAiResponse.model}")
        val json = openAiResponse?.choices?.firstOrNull()?.message?.content ?: ""
        return json
    }
}