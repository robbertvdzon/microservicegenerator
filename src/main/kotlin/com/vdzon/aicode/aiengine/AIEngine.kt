package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

interface AIEngine{
    fun chat(systemPrompt: String, userPrompt: String): String

    fun generateJsonSchema(clazz: Class<*>): String {
        val objectMapper = jacksonObjectMapper()
        val schemaGen = JsonSchemaGenerator(objectMapper)
        val schema: ObjectSchema = schemaGen.generateSchema(clazz) as ObjectSchema
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema)
    }
}