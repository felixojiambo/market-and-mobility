package com.cymelle.app;

import org.springframework.boot.SpringApplication;

public class TestMarketAndMobilityApplication {

	public static void main(String[] args) {
		SpringApplication.from(MarketAndMobilityApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
