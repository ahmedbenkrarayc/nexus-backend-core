package com.nexus.staffing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffingTestController {

    @GetMapping("/test/staffing")
    public String index() {
        return "Staffing module works";
    }
}
