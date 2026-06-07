package com.chatbot.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chatbot.dao.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}