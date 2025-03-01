package com.example.demo

import org.springframework.web.bind.annotation.*
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.beans.factory.annotation.Autowired

@RestController
class UserController {
    @Autowired
    lateinit var userService: UserService

    @PostMapping("/addUser")
    fun addUser(@RequestParam username: String): String {
        userService.addUsername(username)
        return "User added"
    }
}

@Controller
class WebController {
    @Autowired
    lateinit var userService: UserService

    @GetMapping("")
    fun index(model: Model): String {
        model.addAttribute("usernames", userService.getAllUsernames())
        return "index"
    }
}