package com.chatbot;

import com.chatbot.common.HttpStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
@DisplayName("应用上下文集成测试")
class ChatbotApplicationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("应用上下文应成功加载")
    void contextLoads() {
        assertNotNull(context);
        assertTrue(context.getBeanDefinitionCount() > 0);
    }

    @Test
    @DisplayName("ChatbotApplication Bean应存在")
    void mainApplicationBeanExists() {
        assertTrue(context.containsBean("chatbotApplication"));
    }

    @Test
    @DisplayName("数据源应正确配置")
    void dataSourceConfigured() {
        assertTrue(context.containsBean("dataSource"));
        DataSource ds = context.getBean(DataSource.class);
        assertNotNull(ds);
    }

    @Test
    @DisplayName("RedisTemplate应正确配置")
    void redisTemplateConfigured() {
        assertTrue(context.containsBean("redisTemplate"));
    }

    @Test
    @DisplayName("PasswordEncoder Bean应存在")
    void passwordEncoderBeanExists() {
        assertTrue(context.containsBean("passwordEncoder"));
        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        assertNotNull(encoder);
        String encoded = encoder.encode("test123");
        assertTrue(encoder.matches("test123", encoded));
    }

    @Test
    @DisplayName("SecurityFilterChain应配置成功")
    void securityFilterChainConfigured() {
        assertTrue(context.containsBean("securityFilterChain"));
    }

    @Test
    @DisplayName("Swagger OpenAPI Bean应配置成功")
    void swaggerOpenApiConfigured() {
        assertTrue(context.containsBean("chatbotOpenAPI"));
    }

    @Test
    @DisplayName("所有Controller Bean应注册成功")
    void allControllersRegistered() {
        assertTrue(context.containsBean("authController"));
        assertTrue(context.containsBean("chatController"));
        assertTrue(context.containsBean("sessionController"));
        assertTrue(context.containsBean("ticketController"));
        assertTrue(context.containsBean("knowledgeController"));
        assertTrue(context.containsBean("agentController"));
        assertTrue(context.containsBean("analyticsController"));
        assertTrue(context.containsBean("systemController"));
    }

    @Test
    @DisplayName("所有Mapper Bean应注册成功")
    void allMappersRegistered() {
        assertTrue(context.containsBean("sysUserMapper"));
        assertTrue(context.containsBean("chatSessionMapper"));
        assertTrue(context.containsBean("chatMessageMapper"));
        assertTrue(context.containsBean("ticketMapper"));
        assertTrue(context.containsBean("ticketRecordMapper"));
        assertTrue(context.containsBean("knowledgeDocMapper"));
        assertTrue(context.containsBean("knowledgeCategoryMapper"));
    }

    @Test
    @DisplayName("HttpStatusEnum应包含所有必要的状态码")
    void httpStatusEnumComplete() {
        assertEquals(200, HttpStatusEnum.OK.getCode());
        assertEquals(500, HttpStatusEnum.INTERNAL_ERROR.getCode());
        assertEquals(401, HttpStatusEnum.UNAUTHORIZED.getCode());
    }
}