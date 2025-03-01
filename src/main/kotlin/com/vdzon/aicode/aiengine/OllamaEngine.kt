package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.model.AIRequest
import com.vdzon.aicode.model.AiResponse
import com.vdzon.aicode.model.Message
import com.vdzon.aicode.model.ollama.OllamaResponse
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL


//data class SourceFile2(val path: String, val filename: String, val body: String)
//data class SourceFiles2(val files: List<SourceFile2>)


class OllamaEngine(val model: String) : AIEngine {
    override fun chat(systemPrompt: String, userPrompt: String): AiResponse {
//        val jsonSchema = generateJsonSchemaAsMap(SourceFile2::class.java)
        val jsonSchema = generateJsonSchemaAsMap(AiResponse::class.java)
        val request = AIRequest(
            model = model,
            messages = listOf(
                Message("system", systemPrompt),
                Message("user", userPrompt)
            ),
            format = jsonSchema
        )
        val url = URL("http://localhost:11434/api/chat")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        val requesJson = jacksonObjectMapper().writeValueAsString(request)
        connection.outputStream.use { it.write(requesJson.toByteArray()) }
        val responseJson = connection.inputStream.bufferedReader().use(BufferedReader::readText)
        val ollamaResponse = jacksonObjectMapper().readValue(responseJson, object : TypeReference<OllamaResponse>() {})

        val aiResponse = jacksonObjectMapper().readValue(
            ollamaResponse?.message?.content ?: "",
            object : TypeReference<AiResponse>() {})
        return aiResponse


    }

}