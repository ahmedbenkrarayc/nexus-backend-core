package com.nexus.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationTestController {

    @GetMapping("/test/notification")
    public String index() {
        return "Notification module works";
    }
}
