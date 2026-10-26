package com.boulder.boulder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoulderApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoulderApplication.class, args);
	}

}
