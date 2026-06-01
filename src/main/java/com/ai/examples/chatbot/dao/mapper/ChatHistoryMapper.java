package com.ai.examples.chatbot.dao.mapper;

import com.ai.examples.chatbot.dao.entity.ChatHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    List<ChatHistory> findBySessionIdOrderByCreateTimeAsc(@Param("sessionId") String sessionId);

    void deleteBySessionId(@Param("sessionId") String sessionId);
}
