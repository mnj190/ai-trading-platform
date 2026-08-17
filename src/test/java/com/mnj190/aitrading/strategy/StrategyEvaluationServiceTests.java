package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StrategyEvaluationServiceTests {

	private final StrategyEvaluationService service = new StrategyEvaluationService();
	private final StrategyRuleConfig config = StrategyRuleConfig.peMeanReversionV1();

	@Test
	void evaluatesPeerAverageDiscountAndDecisionForUniverse() {
		List<StrategyEvaluationResult> results = service.evaluate(
				List.of(
						per("NVDA", "30.0000"),
						per("GOOGL", "50.0000"),
						per("AAPL", "40.0000"),
						per("AMZN", "45.0000"),
						per("MSFT", "35.0000")
				),
				Map.of(
						"NVDA", StrategyStage.NONE,
						"GOOGL", StrategyStage.BUY1
				),
				config
		);

		StrategyEvaluationResult nvda = findByTicker(results, "NVDA");
		assertThat(nvda.peerAveragePer()).isEqualByComparingTo("40.0000");
		assertThat(nvda.peerDiscount()).isEqualByComparingTo("-0.2500");
		assertThat(nvda.currentStage()).isEqualTo(StrategyStage.NONE);
		assertThat(nvda.decision().signal()).isEqualTo(StrategySignal.BUY);
		assertThat(nvda.decision().nextStage()).isEqualTo(StrategyStage.BUY1);

		StrategyEvaluationResult googl = findByTicker(results, "GOOGL");
		assertThat(googl.peerDiscount()).isEqualByComparingTo("0.2500");
		assertThat(googl.currentStage()).isEqualTo(StrategyStage.BUY1);
		assertThat(googl.decision().signal()).isEqualTo(StrategySignal.SELL);
		assertThat(googl.decision().nextStage()).isEqualTo(StrategyStage.NONE);
	}

	@Test
	void defaultsMissingCurrentStageToNone() {
		List<StrategyEvaluationResult> results = service.evaluate(
				List.of(
						per("NVDA", "40.0000"),
						per("GOOGL", "40.0000"),
						per("AAPL", "40.0000"),
						per("AMZN", "40.0000"),
						per("MSFT", "40.0000")
				),
				Map.of(),
				config
		);

		assertThat(results)
				.extracting(StrategyEvaluationResult::currentStage)
				.containsOnly(StrategyStage.NONE);
		assertThat(results)
				.extracting(result -> result.decision().signal())
				.containsOnly(StrategySignal.HOLD);
	}

	private StrategyEvaluationResult findByTicker(List<StrategyEvaluationResult> results, String ticker) {
		return results.stream()
				.filter(result -> result.ticker().equals(ticker))
				.findFirst()
				.orElseThrow();
	}

	private PeerPerInput per(String ticker, String currentPer) {
		return new PeerPerInput(ticker, new BigDecimal(currentPer));
	}
}

