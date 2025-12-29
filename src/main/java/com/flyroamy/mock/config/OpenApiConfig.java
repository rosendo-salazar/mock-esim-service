package com.flyroamy.mock.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Mock Maya eSIM API")
                .version("1.0.0")
                .description("""
                    Mock implementation of the Maya Mobile Connectivity API for testing purposes.

                    This API mimics the Maya Connect+ Platform endpoints for:
                    - eSIM provisioning and management
                    - Bundle/Plan operations
                    - Usage simulation
                    - Webhook testing

                    **Authentication**: Basic Auth with API Key and Secret
                    """)
                .contact(new Contact()
                    .name("RoamyHub Team")
                    .email("dev@roamyhub.com"))
                .license(new License()
                    .name("Private")
                    .url("https://roamyhub.com")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:" + serverPort)
                    .description("Local Development"),
                new Server()
                    .url("https://mock-maya-stage.azurewebsites.net")
                    .description("Azure Staging")))
            .components(new Components()
                .addSecuritySchemes("basicAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("Basic authentication with API Key as username and API Secret as password")))
            .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
