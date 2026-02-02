package com.nexus.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nexusOpenApi() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Nexus Core API")
                        .description("API documentation for Nexus modular monolith services")
                        .version("v1")
                        .contact(new Contact()
                                .name("Nexus Team")
                                .email("support@nexus.local"))
                        .license(new License()
                                .name("Proprietary")));
    }
}
