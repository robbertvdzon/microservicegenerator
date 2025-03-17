package com.vdzon.aicode.aiengine

interface AIEngine {
    fun chat(jsonSchema: Map<String, Any>, systemPrompt: String, userPrompt: String, model: String): String
}