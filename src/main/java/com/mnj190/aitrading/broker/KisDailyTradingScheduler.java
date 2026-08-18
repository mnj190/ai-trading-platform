package com.mnj190.aitrading.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Fires {@link KisDailyTradingService#runOnce()} automatically every US
 * market weekday while the app is left running under the {@code kis-server}
 * profile.
 * <p>
 * Runs shortly after the US regular session opens (not after the prior
 * close) — the closing price used for the discount calculation doesn't
 * change between close and the next open, but order submission requires
 * the market to actually be open. Scheduling in the {@code America/New_York}
 * zone means the trigger automatically follows US daylight saving time
 * instead of drifting by an hour twice a year the way a fixed KST cron
 * would.
 */
@Component
@Profile("kis-server")
class KisDailyTradingScheduler {

	private static final Logger log = LoggerFactory.getLogger(KisDailyTradingScheduler.class);

	private final KisDailyTradingService dailyTradingService;

	KisDailyTradingScheduler(KisDailyTradingService dailyTradingService) {
		this.dailyTradingService = Objects.requireNonNull(dailyTradingService);
	}

	@Scheduled(cron = "${kis.evaluation.schedule-cron:0 35 9 * * MON-FRI}", zone = "America/New_York")
	void runScheduledEvaluation() {
		try {
			dailyTradingService.runOnce();
		}
		catch (Exception ex) {
			log.error("Scheduled KIS daily evaluation failed; will retry on the next scheduled run", ex);
		}
	}
}
