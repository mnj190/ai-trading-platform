package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StrategyEvaluationServiceTests {

	private final StrategyEvaluationService service = new StrategyEvaluationService();
	private final StrategyRuleConfig config = StrategyRuleConfig.peMeanReversionV1();

	@Test
	void evaluatesNormalizedPerDiscountAndEntryDecisionForUniverse() {
		StrategyEvaluation evaluation = service.evaluate(
				List.of(
						input("NVDA", "30.0000", "37.5000"),
						input("GOOGL", "50.0000", "50.0000"),
						input("AAPL", "55.0000", "50.0000"),
						input("AMZN", "45.0000", "50.0000"),
						input("MSFT", "60.0000", "50.0000")
				),
				Optional.empty(),
				config
		);

		StrategyEvaluationResult nvda = findByTicker(evaluation.results(), "NVDA");
		assertThat(nvda.normalizedPer()).isEqualByComparingTo("0.8000");
		assertThat(nvda.peerAverageNormalizedPer()).isEqualByComparingTo("1.0000");
		assertThat(nvda.peerDiscount()).isEqualByComparingTo("-0.2000");

		assertThat(evaluation.decision().signal()).isEqualTo(StrategySignal.ENTRY);
		assertThat(evaluation.decision().buyTicker()).isEqualTo("NVDA");
	}

	@Test
	void evaluatesSwitchDecisionForCurrentHolding() {
		StrategyEvaluation evaluation = service.evaluate(
				List.of(
						input("NVDA", "43.0000", "50.0000"),
						input("GOOGL", "50.0000", "50.0000"),
						input("AAPL", "55.0000", "50.0000"),
						input("AMZN", "36.0000", "50.0000"),
						input("MSFT", "60.0000", "50.0000")
				),
				Optional.of("NVDA"),
				config
		);

		assertThat(evaluation.decision().signal()).isEqualTo(StrategySignal.SWITCH);
		assertThat(evaluation.decision().sellTicker()).isEqualTo("NVDA");
		assertThat(evaluation.decision().buyTicker()).isEqualTo("AMZN");
	}

	private StrategyEvaluationResult findByTicker(List<StrategyEvaluationResult> results, String ticker) {
		return results.stream()
				.filter(result -> result.ticker().equals(ticker))
				.findFirst()
				.orElseThrow();
	}

	private StrategyValuationInput input(String ticker, String currentPer, String fiveYearAveragePer) {
		return new StrategyValuationInput(
				ticker,
				new BigDecimal("100.0000"),
				new BigDecimal("5.0000"),
				new BigDecimal(currentPer),
				new BigDecimal(fiveYearAveragePer)
		);
	}
}

