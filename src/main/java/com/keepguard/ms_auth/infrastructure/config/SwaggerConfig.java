package com.keepguard.ms_auth.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KeepGuard Auth API")
                        .version("1.0.0")
                        .description("API de autenticação e gerenciamento de usuários para múltiplas aplicações. " +
                                   "Esta API fornece funcionalidades de autenticação JWT, gerenciamento de usuários, " +
                                   "controle de aplicações e validação de tokens.")
                        .contact(new Contact()
                                .name("KeepGuard Team")
                                .email("suporte@keepguard.com")
                                .url("https://keepguard.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor Docker Local"),
                        new Server()
                                .url("http://localhost:8581")
                                .description("Servidor Local")
                ));
    }
}