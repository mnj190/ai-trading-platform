package com.mnj190.aitrading.broker;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("kis-daily-evaluation")
@Order(1)
class KisDailyEvaluationRunner implements ApplicationRunner {

	private final KisDailyTradingService dailyTradingService;

	KisDailyEvaluationRunner(KisDailyTradingService dailyTradingService) {
		this.dailyTradingService = Objects.requireNonNull(dailyTradingService);
	}

	@Override
	public void run(ApplicationArguments args) {
		dailyTradingService.runOnce();
	}
}
