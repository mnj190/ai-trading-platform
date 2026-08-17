package com.mnj190.aitrading.strategy;

import java.util.Objects;

public record StrategyDecision(StrategySignal signal, StrategyStage nextStage) {

	public StrategyDecision {
		Objects.requireNonNull(signal, "signal must not be null");
		Objects.requireNonNull(nextStage, "nextStage must not be null");
	}

	public static StrategyDecision hold(StrategyStage currentStage) {
		return new StrategyDecision(StrategySignal.HOLD, currentStage);
	}

	public static StrategyDecision buy(StrategyStage nextStage) {
		return new StrategyDecision(StrategySignal.BUY, nextStage);
	}

	public static StrategyDecision sellAll() {
		return new StrategyDecision(StrategySignal.SELL, StrategyStage.NONE);
	}
}

