package com.qianyv.jstartaiagent;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.qianyv.jstartaiagent.mapper")
public class JstartAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(JstartAiAgentApplication.class, args);
    }

}
