package com.eventflow.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventFlow Event Service API")
                        .description("REST API for managing events and ticket types")
                        .version("v1"));
    }
}
