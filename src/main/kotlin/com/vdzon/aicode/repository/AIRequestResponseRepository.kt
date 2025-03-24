package com.vdzon.aicode.repository

import com.vdzon.aicode.model.AIRequestResponse
import org.springframework.data.mongodb.repository.MongoRepository

interface AIRequestResponseRepository : MongoRepository<AIRequestResponse, String>