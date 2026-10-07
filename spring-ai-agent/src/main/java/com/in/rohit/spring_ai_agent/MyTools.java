package com.in.rohit.spring_ai_agent;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MyTools {

    @Tool(description = "Returns the current date and time")
    public String currentTime() {
        return LocalDateTime.now().toString();
    }

    @Tool(description = "Adds two numbers")
    public double add(double a, double b) {
        return a + b;
    }
}
