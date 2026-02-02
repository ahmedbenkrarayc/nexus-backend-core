package com.nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.nexus")
public class NexusBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusBootApplication.class, args);
    }

}
