package com.eastcui.deepseekdemo;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
@MapperScan("com.eastcui.deepseekdemo.demos.dao")
public class DeepseekDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeepseekDemoApplication.class, args);
        log.info("启动成功");
        log.info("http://127.0.0.1:8081");
        log.info("http://127.0.0.1:8081/index2.html");
        log.info("http://127.0.0.1:8081/index3.html");
    }

}
