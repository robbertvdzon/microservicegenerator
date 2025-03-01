package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.model.AiResponse

interface AIEngine {
    fun chat(systemPrompt: String, userPrompt: String): AiResponse

    // Functie om JSON Schema naar een Map te genereren
    fun generateJsonSchemaAsMap(clazz: Class<*>): Map<String, Any> {
        val objectMapper = jacksonObjectMapper()
        val schemaGen = JsonSchemaGenerator(objectMapper)
        val schema: ObjectSchema = schemaGen.generateSchema(clazz) as ObjectSchema

        // Converteer JSON Schema naar een Map met TypeReference
        val jsonString = objectMapper.writeValueAsString(schema)
        return objectMapper.readValue(jsonString, object : TypeReference<Map<String, Any>>() {})
    }
}