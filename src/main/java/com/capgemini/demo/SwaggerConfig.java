package com.capgemini.demo;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server; // VERSIONING: ADDED
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card Payment Request Management Service")
                        .version("1.0") // VERSIONING: ensure version is shown
                        .description("""
                                Documentation for the Card Payment Request Management API.
                                This service supports **versioned** endpoints:
                                - Legacy (unversioned): /
                                - Version 1:            /v1
                                """)) // VERSIONING: ADDED
                // VERSIONING: ADDED — expose both servers in Swagger UI
                .addServersItem(new Server().url("/").description("Legacy (unversioned)"))
                .addServersItem(new Server().url("/v1").description("Version 1"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes(
                        "Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}