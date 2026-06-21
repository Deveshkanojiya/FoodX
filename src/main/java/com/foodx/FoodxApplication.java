package com.foodx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FoodxApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodxApplication.class, args);
    }
}
