package com.chatbot.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chatbot.dao.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}