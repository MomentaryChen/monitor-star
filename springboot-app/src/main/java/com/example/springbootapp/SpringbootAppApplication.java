package com.example.springbootapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootAppApplication.class, args);
    }
}


