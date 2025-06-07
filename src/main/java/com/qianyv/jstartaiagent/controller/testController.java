package com.qianyv.jstartaiagent.controller;

import cn.hutool.core.lang.UUID;
import com.qianyv.jstartaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testController {

    @Resource
    private LoveApp loveApp;

    @GetMapping("/test")
    public String test() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是jstart";
        String answer = loveApp.doChat(message, chatId);
        //断言，如果为空，报错
        assert answer != null;
                // 第二轮
        message = "我的对象是千语，怎么让她跟爱我";
        answer = loveApp.doChat(message, chatId);
        assert answer != null;
        // 第三轮
        message = "还有什么建议吗";
        answer = loveApp.doChat(message, chatId);
        assert answer != null;
        // 第四轮，测试记忆
        message = "我是谁来着，我的对象又是谁来着？刚刚告诉过你的";
        answer = loveApp.doChat(message, chatId);
        assert answer != null;

        return "success";

    }

}
