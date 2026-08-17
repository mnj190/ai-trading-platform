package com.mnj190.aitrading.strategy;

import java.util.Objects;

public record StrategyDecision(StrategySignal signal, String sellTicker, String buyTicker) {

	public StrategyDecision {
		Objects.requireNonNull(signal, "signal must not be null");
	}

	public static StrategyDecision hold() {
		return new StrategyDecision(StrategySignal.HOLD, null, null);
	}

	public static StrategyDecision entry(String buyTicker) {
		return new StrategyDecision(StrategySignal.ENTRY, null, requireTicker(buyTicker, "buyTicker"));
	}

	public static StrategyDecision switchTo(String sellTicker, String buyTicker) {
		return new StrategyDecision(
				StrategySignal.SWITCH,
				requireTicker(sellTicker, "sellTicker"),
				requireTicker(buyTicker, "buyTicker")
		);
	}

	public static StrategyDecision exit(String sellTicker) {
		return new StrategyDecision(StrategySignal.EXIT, requireTicker(sellTicker, "sellTicker"), null);
	}

	private static String requireTicker(String ticker, String name) {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return ticker;
	}
}

