package com.qianyv.jstartaiagent.chatMemory;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.Gson;
import com.qianyv.jstartaiagent.enums.MessageTypeEnum;
import com.qianyv.jstartaiagent.model.domain.ConversationMemory;
import com.qianyv.jstartaiagent.service.ConversationMemoryService;
import com.qianyv.jstartaiagent.service.impl.ConversationMemoryServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @date 2025年6月7日15:51:39
 * @description 基于 MySQL 持久化的对话记忆
 */
@Component
public class MySQLBasedChatMemory implements ChatMemory {

    private final ConversationMemoryService conversationMemoryService;

    public MySQLBasedChatMemory(ConversationMemoryService conversationMemoryService) {
        this.conversationMemoryService = conversationMemoryService;
    }


    @Override
    public void add(String conversationId, List<Message> messages) {
        Gson gson = new Gson();
        List<ConversationMemory> conversationMemories = messages.stream().map(message -> {
            String messageType = message.getMessageType().getValue();
            String mes = gson.toJson(message);
            return ConversationMemory.builder().conversationId(conversationId)
                    .messageType(messageType).memory(mes).build();
        }).toList();
        conversationMemoryService.saveBatch(conversationMemories);
    }
    

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<ConversationMemory> messages = conversationMemoryService.getMessages(conversationId);
        if (CollectionUtil.isEmpty(messages)) {
            return List.of();
        }
        return messages.stream()
                .skip(Math.max(0, messages.size() - lastN))
                .map(this::getMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        conversationMemoryService.deleteMemory(conversationId);
    }

    private Message getMessage(ConversationMemory conversationMemory) {
        String memory = conversationMemory.getMemory();
        Gson gson = new Gson();
        return (Message) gson.fromJson(memory, MessageTypeEnum.fromValue(conversationMemory.getMessageType()).getClazz());
    }
}

