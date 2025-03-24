package com.vdzon.aicode.service

import com.vdzon.aicode.model.AIRequestResponse
import com.vdzon.aicode.repository.AIRequestResponseRepository
import org.springframework.stereotype.Service

@Service
class AIRequestResponseService(
     private val aiRequestResponseRepository: AIRequestResponseRepository
){
    fun logRequestAndResponse(request: String, response: String) {
        aiRequestResponseRepository.save(AIRequestResponse(request = request, response = response))
    }

    fun getAllRequestsAndResponses(): List<AIRequestResponse> = aiRequestResponseRepository.findAll()
}