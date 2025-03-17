package com.vdzon.aicode.aiengine

import com.vdzon.aicode.aiengine.ollama.OllamaEngine
import com.vdzon.aicode.aiengine.openai.OpenAiEngine
import org.springframework.stereotype.Service

@Service
class AiEngineFactory(
    private val openAiEngine: OpenAiEngine,
    private val ollamaEngine: OllamaEngine
) {
    fun getAiEngine(engine: String): AIEngine {
        return when (engine) {
            "OPEN_AI" -> openAiEngine
            "OLLAMA" -> ollamaEngine
            else -> throw IllegalArgumentException("Unknown engine: $engine")
        }
    }

}