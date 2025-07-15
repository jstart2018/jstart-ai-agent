package com.qianyv.jstartaiagent.controller;


import com.qianyv.jstartaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai/love_app")
@Slf4j
public class AiController {

    @Resource
    private LoveApp loveApp;


    @GetMapping(value = "/doChat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(String message, String charId) {
        Flux<String> stringFlux = loveApp.doChatByStream(message, charId);

        // 返回响应
        return stringFlux;
    }

    @GetMapping("/doChat/sse2")
    public Flux<ServerSentEvent<String>> chatBySse2(String message, String charId) {
        Flux<ServerSentEvent<String>> serverSentEventFlux =
                loveApp.doChatByStream(message, charId).map(chunk -> {
                    ServerSentEvent<String> result = ServerSentEvent.<String>builder()
                            .data(chunk)
                            .build();
                    return result;
                });
        return serverSentEventFlux;
    }

    @GetMapping("/doChat/sse3")
    public SseEmitter chatBySseEmitter(String message, String charId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);//超时时间
        loveApp.doChatByStream(message, charId)
                .subscribe(chunk -> {   // 订阅数据流
                            try {
                                sseEmitter.send(chunk);// 发送数据到客户端
                            } catch (Exception e) {
                                sseEmitter.completeWithError(e);// 发送异常时，执行异常完成函数（手动关闭）
                                log.error("发送异常：{}",e);
                            }
                        }, sseEmitter::completeWithError,sseEmitter::complete);
        return sseEmitter;
    }


}
