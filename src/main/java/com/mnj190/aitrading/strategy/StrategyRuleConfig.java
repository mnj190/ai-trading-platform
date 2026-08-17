package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;

public record StrategyRuleConfig(
		BigDecimal entryThreshold,
		BigDecimal switchThreshold,
		BigDecimal exitThreshold,
		int maxPositions
) {

	public static StrategyRuleConfig peMeanReversionV1() {
		return new StrategyRuleConfig(
				new BigDecimal("-0.1500"),
				new BigDecimal("0.0500"),
				new BigDecimal("0.0000"),
				1
		);
	}

	public StrategyRuleConfig {
		validateNotNull(entryThreshold, "entryThreshold");
		validateNotNull(switchThreshold, "switchThreshold");
		validateNotNull(exitThreshold, "exitThreshold");

		if (entryThreshold.compareTo(exitThreshold) >= 0) {
			throw new IllegalArgumentException("entryThreshold must be less than exitThreshold");
		}
		if (switchThreshold.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("switchThreshold must be greater than zero");
		}
		if (maxPositions != 1) {
			throw new IllegalArgumentException("PE_MEAN_REVERSION_V1 supports exactly one position");
		}
	}

	private static void validateNotNull(BigDecimal value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + " must not be null");
		}
	}
}

