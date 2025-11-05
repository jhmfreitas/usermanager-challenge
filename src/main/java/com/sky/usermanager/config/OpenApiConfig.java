package com.sky.usermanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Manager API")
                        .description("REST API for managing users and external projects")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Joao Freitas")
                                .email("joao.h.m.freitas@gmail.com")));
    }
}
