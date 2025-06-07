package com.qianyv.jstartaiagent.app;

import cn.hutool.core.lang.UUID;
import com.qianyv.jstartaiagent.chatMemory.MySQLBasedChatMemory;
import com.qianyv.jstartaiagent.service.ConversationMemoryService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Autowired
    private MySQLBasedChatMemory mysqlBasedChatMemory;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是jstart";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);//断言，如果为空，报错
        // 第二轮
        message = "我的对象是千语，怎么让她跟爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "还有什么建议吗";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第四轮，测试记忆
        message = "我是谁来着，我的对象又是谁来着？刚刚告诉过你的";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatTestRePut() {
        String message = "你好，我是程序员千语。给我一些关于恋爱初期的建议";
        String uuid = UUID.randomUUID().toString();
        LoveApp.LoveReport loveReport = loveApp.doChatTestRePut(message, uuid );
        Assertions.assertNotNull(loveReport);
    }
}
