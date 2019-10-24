package com.yunjisuan.presentation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PresentationApplication {
    public static void main(String[] args) {
        SpringApplication.run(PresentationApplication.class, args);
    }
}
