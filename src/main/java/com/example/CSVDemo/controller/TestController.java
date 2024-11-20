package com.example.CSVDemo.controller;
    
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;

    @Controller
    public class TestController {
    
        @GetMapping("/files/test") 
        public String showTestPage() {
            return "test";
        }
    }
    


