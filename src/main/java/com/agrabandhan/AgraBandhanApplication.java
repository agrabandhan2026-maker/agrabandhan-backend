package com.agrabandhan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgraBandhanApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgraBandhanApplication.class, args);
    }
}
