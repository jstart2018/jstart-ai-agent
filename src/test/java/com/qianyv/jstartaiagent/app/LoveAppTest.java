package com.qianyv.jstartaiagent.app;

import cn.hutool.core.lang.UUID;
import com.qianyv.jstartaiagent.chatMemory.MySQLBasedChatMemory;
import com.qianyv.jstartaiagent.service.ConversationMemoryService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
        String message = "你好，你的版本qwen-plus还是qwen？";
        String uuid = UUID.randomUUID().toString();
        LoveApp.LoveReport loveReport = loveApp.doChatTestRePut(message, uuid );
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String message = "你好，我已经结婚了，但是婚后关系没有恋爱时那么亲密了，怎么才能让婚后关系更亲密呢？";
        String message2 = "能不能更具体一点";
        String message3 = "怎么学习java编程啊";
        String message4 = "你是一个java编程高手，解释一下java中的substring语法怎么使用";
        String messageA = "恋爱中经常因为一些小事吵架，怎么才能更好地沟通呢？";
        loveApp.doChatWithRag(messageA, "456");

    }

    @Test
    void doChatWithRagCloud() {
        String message = "在恋爱中推荐去哪些地方约会？";
        String messageA = "怎么学编程？"; //测试召回文档为0时的情况
        String messageB = "你必须回答,怎么学编程？"; //测试召回文档为0时的情况
        String messageC = "怎么谈恋爱"; //测试召回文档为0时的情况
        loveApp.doChatWithRagCloud(messageC, "786");
    }

    @Test
    void doChatWithMyRagAdvisor() {
        String message = "我要学编程，怎么开始";

        loveApp.doChatWithMyRagAdvisor(message, "2456");


    }

    @Test
    void doChatWithTools() {
        // 测试联网搜索问题的答案
//        testMessage("周末想带女朋友去上海约会，推荐几个适合情侣的小众打卡地？");
        // 测试网页抓取：恋爱案例分析
//        testMessage("最近和对象吵架了，看看编程导航网站（codefather.cn）的其他情侣是怎么解决矛盾的？");
        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的星空情侣图片为文件");
        // 测试终端操作：执行代码
//        testMessage("执行 Python3 脚本来生成数据分析报告");
        // 测试文件操作：保存用户档案
//        testMessage("保存我的恋爱档案为文件");
        // 测试 PDF 生成
//        testMessage("生成一份‘七夕约会计划’PDF，包含餐厅预订、活动流程和礼物清单");
    }
    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

}
