package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeerAverageNormalizedPerCalculatorTests {

	private final PeerAverageNormalizedPerCalculator calculator = new PeerAverageNormalizedPerCalculator();

	@Test
	void calculatesPeerAverageNormalizedPerForFiveTickers() {
		BigDecimal result = calculator.calculate(List.of(
				normalized("NVDA", "0.8000"),
				normalized("GOOGL", "1.0000"),
				normalized("AAPL", "1.1000"),
				normalized("AMZN", "0.9000"),
				normalized("MSFT", "1.2000")
		));

		assertThat(result).isEqualByComparingTo("1.0000");
	}

	@Test
	void normalizesTicker() {
		NormalizedPerInput input = normalized(" nvda ", "0.8000");

		assertThat(input.ticker()).isEqualTo("NVDA");
	}

	@Test
	void rejectsUniverseWithLessThanFiveTickers() {
		assertThatThrownBy(() -> calculator.calculate(List.of(
				normalized("NVDA", "0.8000"),
				normalized("GOOGL", "1.0000")
		))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("exactly 5");
	}

	@Test
	void rejectsDuplicateTicker() {
		assertThatThrownBy(() -> calculator.calculate(List.of(
				normalized("NVDA", "0.8000"),
				normalized("GOOGL", "1.0000"),
				normalized("AAPL", "1.1000"),
				normalized("AMZN", "0.9000"),
				normalized("nvda", "1.2000")
		))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("duplicate ticker");
	}

	@Test
	void rejectsNonPositiveNormalizedPer() {
		assertThatThrownBy(() -> normalized("NVDA", "0.0000"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("greater than zero");
	}

	private NormalizedPerInput normalized(String ticker, String normalizedPer) {
		return new NormalizedPerInput(ticker, new BigDecimal(normalizedPer));
	}
}

