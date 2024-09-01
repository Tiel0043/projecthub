package com.mwkim.projecthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProjecthubApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjecthubApplication.class, args);
	}

}
