package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.Objects;

public record StrategyEvaluationResult(
		String ticker,
		BigDecimal currentPer,
		BigDecimal fiveYearAveragePer,
		BigDecimal normalizedPer,
		BigDecimal peerAverageNormalizedPer,
		BigDecimal peerDiscount
) {

	public StrategyEvaluationResult {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
		Objects.requireNonNull(currentPer, "currentPer must not be null");
		Objects.requireNonNull(fiveYearAveragePer, "fiveYearAveragePer must not be null");
		Objects.requireNonNull(normalizedPer, "normalizedPer must not be null");
		Objects.requireNonNull(peerAverageNormalizedPer, "peerAverageNormalizedPer must not be null");
		Objects.requireNonNull(peerDiscount, "peerDiscount must not be null");
	}
}

