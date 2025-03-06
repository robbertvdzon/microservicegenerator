package com.vdzon.aicode.aiengine

object AiEngineFactory {
    fun getAiEngine(engine: String, model: String): AIEngine {
        return when (engine) {
            "OPEN_AI" -> OpenAiEngine(model)
            "OLLAMA" -> OllamaEngine(model)
            else -> throw IllegalArgumentException("Unknown engine: $engine")
        }
    }

}