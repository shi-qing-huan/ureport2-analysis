package com.bstek.ureport.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * @Description 外部tomat启动
 * @Author hans
 * @CreateDate 2022-9-7
 */
@SpringBootApplication
public class TomcatStart extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(TomcatStart.class);
    }
    public static void main(String[] args) {
        SpringApplication.run(TomcatStart.class, args);
    }
}
