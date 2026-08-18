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
 * profile: evaluates and, if a signal fires, submits in one step, 10
 * minutes before the US regular session closes.
 * <p>
 * This timing is a deliberate compromise: the market is still open, so a
 * signal can be submitted immediately (no next-session wait, no "market
 * closed" rejection), while the quote at that point is close enough to the
 * day's eventual close for the discount calculation to be meaningful.
 * Earlier designs tried computing right after close (correct price, but
 * can't submit until the market reopens — a full day of latency) and right
 * after the next open (can submit immediately, but the discount is priced
 * off a live quote several minutes into the new session, not the prior
 * close). This is close enough to the close to avoid most of that drift,
 * without introducing next-session latency.
 */
@Component
@Profile("kis-server")
class KisDailyTradingScheduler {

	private static final Logger log = LoggerFactory.getLogger(KisDailyTradingScheduler.class);

	private final KisDailyTradingService dailyTradingService;

	KisDailyTradingScheduler(KisDailyTradingService dailyTradingService) {
		this.dailyTradingService = Objects.requireNonNull(dailyTradingService);
	}

	@Scheduled(cron = "${kis.evaluation.schedule-cron:0 50 15 * * MON-FRI}", zone = "America/New_York")
	void runScheduledEvaluation() {
		try {
			dailyTradingService.runOnce();
		}
		catch (Exception ex) {
			log.error("Scheduled KIS daily evaluation failed; will retry on the next scheduled run", ex);
		}
	}
}
