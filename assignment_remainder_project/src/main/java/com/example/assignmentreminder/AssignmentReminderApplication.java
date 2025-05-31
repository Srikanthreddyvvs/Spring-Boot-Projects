package com.example.assignmentreminder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AssignmentReminderApplication {
	public static void main(String[] args) {
		SpringApplication.run(AssignmentReminderApplication.class, args);
	}
}
