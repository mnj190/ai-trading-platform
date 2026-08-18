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
 * profile: evaluates and, if a signal fires, submits in one step, right
 * after the US regular session opens.
 * <p>
 * Note: at this trigger time the market has already been open a few
 * minutes, so the discount is computed off that moment's live quote, not
 * the prior session's settled close. A close-time-evaluate /
 * open-time-submit split (each phase run at the time it's actually valid)
 * was tried and reverted in favor of this simpler single-step design.
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
