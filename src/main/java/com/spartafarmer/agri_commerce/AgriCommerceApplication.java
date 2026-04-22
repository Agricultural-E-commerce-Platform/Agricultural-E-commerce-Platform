package com.spartafarmer.agri_commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AgriCommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgriCommerceApplication.class, args);
	}

}
