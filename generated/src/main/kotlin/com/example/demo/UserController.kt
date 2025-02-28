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

    @PostMapping("/removeUser")
    fun removeUser(@RequestParam username: String): String {
        userService.removeUsername(username)
        return "User removed"
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