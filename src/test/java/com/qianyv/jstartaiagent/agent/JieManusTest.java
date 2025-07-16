package com.qianyv.jstartaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JieManusTest {

    @Resource JieManus jieManus;

    @Test
    public void run(){
        String userPrompt = """  
                帮我寻找一下广州最新适合团建的地方，并给出一些推荐的活动和预算范围，并且给我提供一些场地图片。
                并以 PDF 格式输出资源目录中""";
        String answer = jieManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }



}