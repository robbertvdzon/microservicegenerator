package com.vdzon.aicode.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class AIRequestResponse(
    @Id
    val id: String? = null,
    val request: String,
    val response: String,
    val timestamp: Long = System.currentTimeMillis()
)