package com.nexus.organization.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrganizationTestController {

    @GetMapping("/test/organization")
    public String index() {
        return "Organization module works";
    }
}
