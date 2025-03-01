package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.model.Message
import com.vdzon.aicode.model.AIRequest
import com.vdzon.aicode.model.SourceFiles
import com.vdzon.aicode.model.ollama.OllamaResponse
import com.vdzon.aicode.model.openai.OpenAIResponse
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class OllamaEngine(val model: String): AIEngine {
    override fun chat(systemPrompt: String, userPrompt: String): String {
        val jsonSchema = generateJsonSchemaAsMap(SourceFiles::class.java)
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
        val sourceFilesJson = ollamaResponse?.message?.content ?: ""
        return sourceFilesJson


    }

}