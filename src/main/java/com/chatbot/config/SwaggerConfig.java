package com.chatbot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI chatbotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("通用智能客服AI系统 API")
                        .description("基于Spring AI的智能客服系统接口文档")
                        .version("v2.0.0")
                        .contact(new Contact()
                                .name("Chatbot Team")
                                .email("chatbot@example.com")));
    }
}