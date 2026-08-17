package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeerAveragePerCalculatorTests {

	private final PeerAveragePerCalculator calculator = new PeerAveragePerCalculator();

	@Test
	void calculatesPeerAveragePerForFiveTickers() {
		BigDecimal result = calculator.calculate(List.of(
				per("NVDA", "45.0000"),
				per("GOOGL", "35.0000"),
				per("AAPL", "30.0000"),
				per("AMZN", "50.0000"),
				per("MSFT", "40.0000")
		));

		assertThat(result).isEqualByComparingTo("40.0000");
	}

	@Test
	void normalizesTicker() {
		PeerPerInput input = per(" nvda ", "45.0000");

		assertThat(input.ticker()).isEqualTo("NVDA");
	}

	@Test
	void rejectsUniverseWithLessThanFiveTickers() {
		assertThatThrownBy(() -> calculator.calculate(List.of(
				per("NVDA", "45.0000"),
				per("GOOGL", "35.0000")
		))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("exactly 5");
	}

	@Test
	void rejectsDuplicateTicker() {
		assertThatThrownBy(() -> calculator.calculate(List.of(
				per("NVDA", "45.0000"),
				per("GOOGL", "35.0000"),
				per("AAPL", "30.0000"),
				per("AMZN", "50.0000"),
				per("nvda", "40.0000")
		))).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("duplicate ticker");
	}

	@Test
	void rejectsNonPositiveCurrentPer() {
		assertThatThrownBy(() -> per("NVDA", "0.0000"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("greater than zero");
	}

	private PeerPerInput per(String ticker, String currentPer) {
		return new PeerPerInput(ticker, new BigDecimal(currentPer));
	}
}

