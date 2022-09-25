package com.fqm.test.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TestCacheApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestCacheApplication.class, args);
    }

}
