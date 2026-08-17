package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.Locale;

public record StrategyValuationInput(
		String ticker,
		BigDecimal closePrice,
		BigDecimal ttmEps,
		BigDecimal currentPer
) {

	public StrategyValuationInput {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
		validatePositive(closePrice, "closePrice");
		validatePositive(ttmEps, "ttmEps");
		validatePositive(currentPer, "currentPer");

		ticker = ticker.trim().toUpperCase(Locale.ROOT);
	}

	PeerPerInput toPeerPerInput() {
		return new PeerPerInput(ticker, currentPer);
	}

	private static void validatePositive(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
	}
}

