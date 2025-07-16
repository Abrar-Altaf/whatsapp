package com.whatsapp.whatsapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${SWAGGER_SERVER_URL:http://localhost:8080}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl(swaggerServerUrl);
        return new OpenAPI().servers(List.of(server));
    }
} 