package com.nexus.employee.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AuthServiceProperties.class)
public class EmployeeClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient authServiceHttpClient(RestClient.Builder builder, AuthServiceProperties properties) {
        String configuredBaseUrl = properties.baseUrl();
        String normalizedBaseUrl = (configuredBaseUrl == null || configuredBaseUrl.isBlank())
            ? "http://localhost:8081"
            : configuredBaseUrl;

        if (!normalizedBaseUrl.startsWith("http://") && !normalizedBaseUrl.startsWith("https://")) {
            normalizedBaseUrl = "http://" + normalizedBaseUrl;
        }

        return builder
            .baseUrl(normalizedBaseUrl)
                .build();
    }
}