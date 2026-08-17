package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeerDiscountCalculatorTests {

	private final PeerDiscountCalculator calculator = new PeerDiscountCalculator();

	@Test
	void calculatesDiscountBelowPeerAverage() {
		BigDecimal result = calculator.calculate(new BigDecimal("40.0000"), new BigDecimal("50.0000"));

		assertThat(result).isEqualByComparingTo("-0.2000");
	}

	@Test
	void calculatesZeroWhenCurrentPerEqualsPeerAverage() {
		BigDecimal result = calculator.calculate(new BigDecimal("50.0000"), new BigDecimal("50.0000"));

		assertThat(result).isEqualByComparingTo("0.0000");
	}

	@Test
	void calculatesPremiumAbovePeerAverage() {
		BigDecimal result = calculator.calculate(new BigDecimal("55.0000"), new BigDecimal("50.0000"));

		assertThat(result).isEqualByComparingTo("0.1000");
	}

	@Test
	void rejectsNonPositiveNormalizedPer() {
		assertThatThrownBy(() -> calculator.calculate(BigDecimal.ZERO, new BigDecimal("50.0000")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("normalizedPer");
	}

	@Test
	void rejectsNonPositivePeerAverageNormalizedPer() {
		assertThatThrownBy(() -> calculator.calculate(new BigDecimal("50.0000"), BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("peerAverageNormalizedPer");
	}
}
