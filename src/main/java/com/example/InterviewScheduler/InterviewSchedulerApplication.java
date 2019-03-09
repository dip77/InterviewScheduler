package com.example.InterviewScheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class InterviewSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(InterviewSchedulerApplication.class, args);
	}

}
