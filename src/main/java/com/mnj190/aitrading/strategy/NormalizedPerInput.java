package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.Locale;

public record NormalizedPerInput(String ticker, BigDecimal normalizedPer) {

	public NormalizedPerInput {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
		if (normalizedPer == null || normalizedPer.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("normalizedPer must be greater than zero");
		}

		ticker = ticker.trim().toUpperCase(Locale.ROOT);
	}
}

