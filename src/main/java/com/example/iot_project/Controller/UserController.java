package com.example.iot_project.Controller;

import com.example.iot_project.Service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    UserService userService;

    @RequestMapping("/")
    public String home() {
        return "Hello World!";
    }
}
