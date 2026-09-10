package ru.netology.threads;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ThreadsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThreadsDemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner run(ThreadDemoService threadDemoService) {
        return args -> threadDemoService.runDemo();
    }
}
