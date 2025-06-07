package com.qianyv.jstartaiagent.service;

import com.qianyv.jstartaiagent.model.domain.ConversationMemory;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 28435
* @description 针对表【conversation_memory】的数据库操作Service
* @createDate 2025-06-07 15:26:39
*/
public interface ConversationMemoryService extends IService<ConversationMemory> {

    List<ConversationMemory> getMessages(String conversationId);


    public boolean deleteMemory(String conversationId);

}
