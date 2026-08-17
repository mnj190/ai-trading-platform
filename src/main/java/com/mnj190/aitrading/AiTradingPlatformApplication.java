package com.mnj190.aitrading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiTradingPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiTradingPlatformApplication.class, args);
	}

}
