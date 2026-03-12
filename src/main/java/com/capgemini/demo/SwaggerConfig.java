package com.capgemini.demo;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;   // VERSIONING: ADDED
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Card Payment Request Management Service")
                        .version("1.0")
                        .description("""
                                This API supports versioned routes:
                                - Legacy (unversioned): /
                                - Version 1:            /v1
                                - Version 2:            /v2

                                You can also use vendor media types:
                                - Accept: application/vnd.cardops.v1+json
                                - Accept: application/vnd.cardops.v2+json
                                """))
                // VERSIONING: ADDED — multiple servers for Swagger UI
                .addServersItem(new Server().url("/").description("Legacy (unversioned)"))
                .addServersItem(new Server().url("/v1").description("Version 1"))
                .addServersItem(new Server().url("/v2").description("Version 2"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes
                        ("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}