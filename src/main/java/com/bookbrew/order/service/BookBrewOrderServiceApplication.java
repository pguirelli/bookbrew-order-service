package com.bookbrew.order.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BookBrewOrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookBrewOrderServiceApplication.class, args);
	}

}
