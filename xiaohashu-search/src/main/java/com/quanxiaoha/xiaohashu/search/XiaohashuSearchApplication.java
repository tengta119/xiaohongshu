package com.quanxiaoha.xiaohashu.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XiaohashuSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(XiaohashuSearchApplication.class, args);
    }

}
