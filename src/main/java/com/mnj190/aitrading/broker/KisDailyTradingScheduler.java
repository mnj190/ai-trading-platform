package com.mnj190.aitrading.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Fires {@link KisDailyTradingService}'s two phases at the times each is
 * actually valid, while the app is left running under the
 * {@code kis-server} profile:
 * <ul>
 *     <li>{@link #runCloseEvaluation()} — shortly after the US regular
 *     session closes, while current-price-detail still reflects that
 *     session's close. Computes the discount and creates a REQUESTED order
 *     if the signal fires. Does not submit anything.</li>
 *     <li>{@link #runOpenSubmission()} — shortly after the US regular
 *     session opens next, when an order can actually be placed. Re-checks
 *     any REQUESTED order against a fresh live quote before submitting.</li>
 * </ul>
 * Both are scheduled in the {@code America/New_York} zone so they follow US
 * daylight saving time automatically instead of drifting by an hour twice a
 * year the way a fixed KST cron would. Both run daily (not just weekdays) —
 * re-running on a US non-trading day is a harmless no-op: valuation_snapshot
 * for that date just gets overwritten with the same numbers, and there are
 * no pending orders for the open-submission phase to pick up.
 */
@Component
@Profile("kis-server")
class KisDailyTradingScheduler {

	private static final Logger log = LoggerFactory.getLogger(KisDailyTradingScheduler.class);

	private final KisDailyTradingService dailyTradingService;

	KisDailyTradingScheduler(KisDailyTradingService dailyTradingService) {
		this.dailyTradingService = Objects.requireNonNull(dailyTradingService);
	}

	@Scheduled(cron = "${kis.evaluation.close-schedule-cron:0 5 16 * * MON-FRI}", zone = "America/New_York")
	void runCloseEvaluation() {
		try {
			dailyTradingService.evaluateAndRequest();
		}
		catch (Exception ex) {
			log.error("Scheduled KIS close-based evaluation failed; will retry on the next scheduled run", ex);
		}
	}

	@Scheduled(cron = "${kis.evaluation.open-schedule-cron:0 35 9 * * MON-FRI}", zone = "America/New_York")
	void runOpenSubmission() {
		try {
			dailyTradingService.submitPendingOrders();
		}
		catch (Exception ex) {
			log.error("Scheduled KIS open-based order submission failed; will retry on the next scheduled run", ex);
		}
	}
}
