package com.healtbot;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class HealthBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(HealthBotApplication.class, args);
	}
	
	@GetMapping("/")
    public String getInfo(HttpServletRequest request) {
        return getHealth(request) + "<br/><br/><a href='/swagger-ui.html'>API documentation</a>";
    }
    
    @GetMapping("/health")
    public String getHealth(HttpServletRequest request) {
        return "healthBot ";
    }

}
