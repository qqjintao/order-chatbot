package com.chatbot.module.chat;

import com.chatbot.TestConfig;
import com.chatbot.module.chat.dto.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@DisplayName("聊天Controller集成测试")
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("sendMessage - 应返回200且包含回复")
    void testSendMessage() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("你好");
        request.setSessionId("test-session-001");

        mockMvc.perform(post("/api/v1/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("sendMessage - 空消息应返回400")
    void testSendMessageWithEmptyMessage() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("");

        mockMvc.perform(post("/api/v1/chat/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("streamMessage - 应返回SSE格式")
    void testStreamMessage() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("测试流式");
        request.setSessionId("test-stream-001");

        mockMvc.perform(post("/api/v1/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("getHistory - 应返回200")
    void testGetHistory() throws Exception {
        mockMvc.perform(get("/api/v1/chat/history")
                        .param("sessionId", "test-session-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("getHistory - 无sessionId参数应返回400")
    void testGetHistoryWithoutSessionId() throws Exception {
        mockMvc.perform(get("/api/v1/chat/history"))
                .andExpect(status().isBadRequest());
    }
}