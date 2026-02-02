package com.nexus.employee.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeTestController {

    @GetMapping("/test/employee")
    public String test() {
        return "employee module works";
    }
}
