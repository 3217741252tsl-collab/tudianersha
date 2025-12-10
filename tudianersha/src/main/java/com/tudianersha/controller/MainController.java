package com.tudianersha.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    
    @GetMapping("/")
    public String home() {
        return "Welcome to the Tudianersha System!";
    }
    
    @GetMapping("/health")
    public String healthCheck() {
        return "System is running healthy";
    }
}