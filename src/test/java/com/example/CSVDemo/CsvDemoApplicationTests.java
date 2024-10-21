package com.example.CSVDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.CSVDemo.service"}) // Specify where to scan for components
public class CsvDemoApplicationTests {
    public static void main(String[] args) {
        SpringApplication.run(CsvDemoApplicationTests.class, args);
    }
}

