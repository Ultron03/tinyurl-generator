package com.tinyurl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TinyurlGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TinyurlGeneratorApplication.class, args);
		System.out.println("TinyURL Generator Application started successfully.");
	}

}
