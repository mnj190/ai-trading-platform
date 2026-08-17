package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PeerDiscountCalculator {

	private static final int RESULT_SCALE = 4;

	public BigDecimal calculate(BigDecimal normalizedPer, BigDecimal peerAverageNormalizedPer) {
		validatePositive(normalizedPer, "normalizedPer");
		validatePositive(peerAverageNormalizedPer, "peerAverageNormalizedPer");

		return normalizedPer
				.divide(peerAverageNormalizedPer, RESULT_SCALE + 4, RoundingMode.HALF_UP)
				.subtract(BigDecimal.ONE)
				.setScale(RESULT_SCALE, RoundingMode.HALF_UP);
	}

	private void validatePositive(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
	}
}
