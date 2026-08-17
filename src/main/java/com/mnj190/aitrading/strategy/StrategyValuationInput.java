package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public record StrategyValuationInput(
		String ticker,
		BigDecimal closePrice,
		BigDecimal ttmEps,
		BigDecimal currentPer,
		BigDecimal fiveYearAveragePer
) {

	private static final int RESULT_SCALE = 4;

	public StrategyValuationInput {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
		validatePositive(closePrice, "closePrice");
		validatePositive(ttmEps, "ttmEps");
		validatePositive(currentPer, "currentPer");
		validatePositive(fiveYearAveragePer, "fiveYearAveragePer");

		ticker = ticker.trim().toUpperCase(Locale.ROOT);
	}

	NormalizedPerInput toNormalizedPerInput() {
		return new NormalizedPerInput(ticker, normalizedPer());
	}

	public BigDecimal normalizedPer() {
		return currentPer
				.divide(fiveYearAveragePer, RESULT_SCALE, RoundingMode.HALF_UP);
	}

	private static void validatePositive(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
	}
}

