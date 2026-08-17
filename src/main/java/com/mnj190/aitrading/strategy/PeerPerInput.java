package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.Locale;

public record PeerPerInput(String ticker, BigDecimal currentPer) {

	public PeerPerInput {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
		if (currentPer == null || currentPer.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("currentPer must be greater than zero");
		}

		ticker = ticker.trim().toUpperCase(Locale.ROOT);
	}
}

