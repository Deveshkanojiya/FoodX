package com.foodx;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/test")
    public String test() {
        return "CI/CD Working V2";
    }
}