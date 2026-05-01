package com.tinyurl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TinyurlGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TinyurlGeneratorApplication.class, args);
		System.out.println("TinyURL Generator Application started successfully.");
	}

}
