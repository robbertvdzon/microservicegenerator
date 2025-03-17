package com.vdzon.aicode.aiengine.ollama

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
class OllamaEngine() : AIEngine {
    override fun chat(jsonSchema: Map<String, Any>, systemPrompt: String, userPrompt: String, model: String): String {
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
        try {
            connection.outputStream.use { it.write(requesJson.toByteArray()) }
            val responseJson = connection.inputStream.bufferedReader().use(BufferedReader::readText)
            val ollamaResponse =
                jacksonObjectMapper().readValue(responseJson, object : TypeReference<OllamaResponse>() {})
            println("Ollama: promptEvalCount:${ollamaResponse.promptEvalCount} evalCount:${ollamaResponse.evalCount}  model: ${ollamaResponse.model}")
            val json = ollamaResponse?.message?.content ?: ""
            return json
        }
        catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}