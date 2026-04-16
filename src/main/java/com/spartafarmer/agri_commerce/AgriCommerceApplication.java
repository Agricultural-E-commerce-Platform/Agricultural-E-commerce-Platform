package com.spartafarmer.agri_commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class AgriCommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgriCommerceApplication.class, args);
	}

}
