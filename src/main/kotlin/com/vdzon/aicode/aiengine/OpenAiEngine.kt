package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vdzon.aicode.model.Message
import com.vdzon.aicode.model.OllamaRequest
import com.vdzon.aicode.model.OpenAIResponse
import com.vdzon.aicode.model.SourceFiles
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

class OpenAiEngine(val model: String): AIEngine {
    override fun chat(systemPrompt: String, userPrompt: String): String {
        val jsonSchema = generateJsonSchema(SourceFiles::class.java)

        val request = OllamaRequest(
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
        val openAiResponse = jacksonObjectMapper().readValue<OpenAIResponse>(responseJson)
        val sourceFilesJson = openAiResponse?.choices?.firstOrNull()?.message?.content ?: ""
        return sourceFilesJson

    }

}