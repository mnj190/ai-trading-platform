package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.Objects;

public record StrategyEvaluationResult(
		String ticker,
		BigDecimal currentPer,
		BigDecimal peerAveragePer,
		BigDecimal peerDiscount,
		StrategyStage currentStage,
		StrategyDecision decision
) {

	public StrategyEvaluationResult {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
		Objects.requireNonNull(currentPer, "currentPer must not be null");
		Objects.requireNonNull(peerAveragePer, "peerAveragePer must not be null");
		Objects.requireNonNull(peerDiscount, "peerDiscount must not be null");
		Objects.requireNonNull(currentStage, "currentStage must not be null");
		Objects.requireNonNull(decision, "decision must not be null");
	}
}

