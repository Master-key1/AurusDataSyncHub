package com.auruspay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AurusDataSyncHubApplication {

	public static void main(String[] args) {
		   System.setProperty("PID", String.valueOf(ProcessHandle.current().pid()));
		SpringApplication.run(AurusDataSyncHubApplication.class, args);
	}

}
