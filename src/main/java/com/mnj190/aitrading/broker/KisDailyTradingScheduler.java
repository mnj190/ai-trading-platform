package com.mnj190.aitrading.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Fires {@link KisDailyTradingService#runOnce()} automatically every day
 * while the app is left running under the {@code kis-server} profile.
 * Runs daily (not just weekdays) — re-evaluating a US non-trading day is a
 * harmless no-op: valuation_snapshot for that date just gets overwritten
 * with the same numbers, and the in-flight order guard in
 * OrderRequestService prevents duplicate submissions.
 */
@Component
@Profile("kis-server")
class KisDailyTradingScheduler {

	private static final Logger log = LoggerFactory.getLogger(KisDailyTradingScheduler.class);

	private final KisDailyTradingService dailyTradingService;

	KisDailyTradingScheduler(KisDailyTradingService dailyTradingService) {
		this.dailyTradingService = Objects.requireNonNull(dailyTradingService);
	}

	@Scheduled(cron = "${kis.evaluation.schedule-cron:0 30 7 * * *}", zone = "Asia/Seoul")
	void runScheduledEvaluation() {
		try {
			dailyTradingService.runOnce();
		}
		catch (Exception ex) {
			log.error("Scheduled KIS daily evaluation failed; will retry on the next scheduled run", ex);
		}
	}
}
