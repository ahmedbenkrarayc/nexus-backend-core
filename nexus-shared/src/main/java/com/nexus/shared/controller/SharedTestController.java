package com.nexus.shared.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SharedTestController {

    @GetMapping("/test/shared")
    public String index() {
        return "Shared module works";
    }
}