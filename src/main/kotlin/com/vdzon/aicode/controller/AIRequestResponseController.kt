package com.vdzon.aicode.controller

import com.vdzon.aicode.model.AIRequestResponse
import com.vdzon.aicode.service.AIRequestResponseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/ailogs")
class AIRequestResponseController(
   private val aiRequestResponseService: AIRequestResponseService
){
   @GetMapping
   fun getAllLogs(): List<AIRequestResponse> = aiRequestResponseService.getAllRequestsAndResponses()
}