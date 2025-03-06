package com.vdzon.aicode.aiengine

import com.vdzon.aicode.aiengine.ollama.OllamaEngine
import com.vdzon.aicode.aiengine.openai.OpenAiEngine

object AiEngineFactory {
    fun getAiEngine(engine: String, model: String): AIEngine {
        return when (engine) {
            "OPEN_AI" -> OpenAiEngine(model)
            "OLLAMA" -> OllamaEngine(model)
            else -> throw IllegalArgumentException("Unknown engine: $engine")
        }
    }

}