package com.example.chatwithai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ChatWithAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatWithAiApplication.class, args);
	}

}

