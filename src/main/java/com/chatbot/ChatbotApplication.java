package com.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class ChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatbotApplication.class, args);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> readyEventListener(Environment env) {
        return event -> {
            String port = env.getProperty("server.port", "8080");
            System.out.println("\n========================================");
            System.out.println("  通用智能客服AI系统 v2.0 启动成功!");
            System.out.println("  API 文档: http://localhost:" + port + "/swagger-ui.html");
            System.out.println("  管理后台: http://localhost:5173/admin");
            System.out.println("========================================\n");
        };
    }
}