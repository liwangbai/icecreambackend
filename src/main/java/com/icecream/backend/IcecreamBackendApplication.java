package com.icecream.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IcecreamBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IcecreamBackendApplication.class, args);
    }
}