package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;

public record StrategyRuleConfig(
		BigDecimal buy1Threshold,
		BigDecimal buy2Threshold,
		BigDecimal buy3Threshold,
		BigDecimal buyUnitRatio,
		BigDecimal sellThreshold
) {

	public static StrategyRuleConfig peMeanReversionV1() {
		return new StrategyRuleConfig(
				new BigDecimal("-0.1500"),
				new BigDecimal("-0.2000"),
				new BigDecimal("-0.2500"),
				new BigDecimal("0.1000"),
				new BigDecimal("0.0000")
		);
	}

	public StrategyRuleConfig {
		validateNotNull(buy1Threshold, "buy1Threshold");
		validateNotNull(buy2Threshold, "buy2Threshold");
		validateNotNull(buy3Threshold, "buy3Threshold");
		validateNotNull(buyUnitRatio, "buyUnitRatio");
		validateNotNull(sellThreshold, "sellThreshold");

		if (buy3Threshold.compareTo(buy2Threshold) > 0 || buy2Threshold.compareTo(buy1Threshold) > 0) {
			throw new IllegalArgumentException("buy thresholds must satisfy BUY3 <= BUY2 <= BUY1");
		}
		if (buyUnitRatio.compareTo(BigDecimal.ZERO) <= 0 || buyUnitRatio.compareTo(BigDecimal.ONE) > 0) {
			throw new IllegalArgumentException("buyUnitRatio must be greater than zero and less than or equal to one");
		}
	}

	private static void validateNotNull(BigDecimal value, String name) {
		if (value == null) {
			throw new IllegalArgumentException(name + " must not be null");
		}
	}
}

