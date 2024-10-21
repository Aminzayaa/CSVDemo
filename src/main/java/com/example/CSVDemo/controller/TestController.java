package com.example.CSVDemo.controller;
    
    import org.springframework.stereotype.Controller;
    import org.springframework.web.bind.annotation.GetMapping;

    @Controller
    public class TestController {
    
        @GetMapping("/files/test") // This mapping will trigger the controller
        public String showTestPage() {
            return "test"; // This returns test.jsp located in /WEB-INF/views/
        }
    }
    


