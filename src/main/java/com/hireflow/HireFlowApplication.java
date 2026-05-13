package com.hireflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class HireFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(HireFlowApplication.class, args);
	}

}
