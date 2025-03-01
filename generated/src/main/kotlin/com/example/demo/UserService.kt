package com.example.demo

import org.springframework.stereotype.Service

@Service
class UserService {
    private val usernames = mutableListOf<String>()

    fun addUsername(username: String) {
        usernames.add(username)
    }

    fun getAllUsernames(): List<String> {
        return usernames
    }
}