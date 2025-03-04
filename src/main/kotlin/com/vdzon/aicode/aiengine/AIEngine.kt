package com.vdzon.aicode.aiengine

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vdzon.aicode.model.AiResponse

interface AIEngine {
    fun chat(systemPrompt: String, userPrompt: String): AiResponse

    data class Schema(
        val id: String,
        val properties: Map<String, Any>,
    )

    private fun addRequired(
        map: Map<String, Any>
    ): Map<String, Any> {
        if(map.containsKey("properties") && !map.containsKey("required")) {
            val props = map.get("properties") as Map<String, Any>
            val requiredFields = props.map { it.key }
            val map2 = map.plus(mapOf("required" to requiredFields))
            return addRequired(map2)
        }
        else {
            val rr = map.map{(key, value) ->
                if (value is Map<*, *>) {
                    key to addRequired(value as Map<String,Any>)
                }
                else key to value
            }
            return rr.toMap()
        }
    }



    private fun convertSchema(
        map: Map<String, Any>,
        schemas: List<Schema>
    ): Map<String, Any> {
        return map.map { (key, value) ->
            var newKey = key
            val rr = when {
                value is Map<*, *> -> {
                    convertSchema(value as Map<String, Any>, schemas)
                } // Recursief aanroepen
                key.contains("ref") -> {
                    val refs = schemas.firstOrNull { it.id==value }?.properties?:emptyMap()
                    newKey = "properties"
                    refs

                } // Vervang "$ref" door "DUMMY"
                else -> value // Laat overige waarden onveranderd
            }
            newKey to rr
        }.toMap()
    }

    fun findAllSchemas(jsonSchema: Map<String, Any>): List<Schema> {
        val type = jsonSchema["type"].toString()
        if (type == "object") {
            val id = jsonSchema.get("id") // heeft props
            if (id != null) {
                val properties = jsonSchema["properties"]
                if (properties is Map<*, *>) {
                    val schema = Schema(id as String, properties as Map<String, Any>)
                    val otherSchemas = findAllSchemas(properties)
                    return otherSchemas.plus(schema)
                }
            }
            val ref = jsonSchema["\$ref"] // zoek oude
            if (ref != null) {
                return emptyList()
            }
        }
        if (type == "array") {
            val items = jsonSchema.get("items") // heeft props
            if (items != null) {
                return findAllSchemas(items as Map<String, Any>)
            }
        }
        val res = jsonSchema.values.flatMap {
            if (it is Map<*, *>) {
                findAllSchemas(it  as Map<String, Any>)
            }
            else emptyList()
        }
        return res
    }

    fun generateJsonSchemaAsMap(clazz: Class<*>): Map<String, Any> {
        val objectMapper = jacksonObjectMapper()
        val schemaGen = JsonSchemaGenerator(objectMapper)
        val schema: ObjectSchema = schemaGen.generateSchema(clazz) as ObjectSchema

        // Converteer JSON Schema naar een Map met TypeReference
        val jsonString = objectMapper.writeValueAsString(schema)
        val jsonSchema =  objectMapper.readValue(jsonString, object : TypeReference<Map<String, Any>>() {})
        val schemas = findAllSchemas(jsonSchema)
        val newSchema: Map<String, Any> = convertSchema(jsonSchema, schemas)
        val newSchema2: Map<String, Any> = addRequired(newSchema)

        return newSchema2
    }
}