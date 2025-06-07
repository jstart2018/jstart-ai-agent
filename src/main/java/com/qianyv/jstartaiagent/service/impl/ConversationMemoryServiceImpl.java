package com.qianyv.jstartaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qianyv.jstartaiagent.model.domain.ConversationMemory;
import com.qianyv.jstartaiagent.service.ConversationMemoryService;
import com.qianyv.jstartaiagent.mapper.ConversationMemoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 28435
* @description 针对表【conversation_memory】的数据库操作Service实现
* @createDate 2025-06-07 15:26:39
*/
@Service
public class ConversationMemoryServiceImpl extends ServiceImpl<ConversationMemoryMapper, ConversationMemory>
    implements ConversationMemoryService{

    /**
     * 获取会话消息
     * @param conversationId 根据id获取
     * @return 获取一整行内容
     */
    public List<ConversationMemory> getMessages(String conversationId) {
        return this.lambdaQuery()
                .eq(ConversationMemory::getConversationId, conversationId)
                .list();
    }

    /**
     * 删除会话
     * @param conversationId 根据id删除
     * @return 成功与否
     */
    public boolean deleteMemory(String conversationId) {
        return this.lambdaUpdate()
                .eq(ConversationMemory::getConversationId, conversationId)
                .remove();
    }
}




