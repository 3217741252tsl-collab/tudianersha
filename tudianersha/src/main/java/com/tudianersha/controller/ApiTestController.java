package com.tudianersha.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class ApiTestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Tudianersha System is running!";
    }
}