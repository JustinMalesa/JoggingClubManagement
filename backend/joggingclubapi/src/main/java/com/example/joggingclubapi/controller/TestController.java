package com.example.joggingclubapi.controller;

/**
 *
 * @author justi
 */

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String testConnection() {
        return "Jogging Club API is running!";
    }
}