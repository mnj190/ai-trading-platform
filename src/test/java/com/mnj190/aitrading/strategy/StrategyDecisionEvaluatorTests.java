package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrategyDecisionEvaluatorTests {

	private final StrategyDecisionEvaluator evaluator = new StrategyDecisionEvaluator();
	private final StrategyRuleConfig config = StrategyRuleConfig.peMeanReversionV1();

	@Test
	void entersBestCandidateWhenCashAndDiscountReachesEntryThreshold() {
		StrategyDecision decision = evaluator.evaluate(
				List.of(
						result("NVDA", "-0.1000"),
						result("AMZN", "-0.1500"),
						result("AAPL", "0.0000")
				),
				Optional.empty(),
				config
		);

		assertThat(decision.signal()).isEqualTo(StrategySignal.ENTRY);
		assertThat(decision.buyTicker()).isEqualTo("AMZN");
	}

	@Test
	void holdsCashWhenBestCandidateDoesNotReachEntryThreshold() {
		StrategyDecision decision = evaluator.evaluate(
				List.of(result("NVDA", "-0.1499")),
				Optional.empty(),
				config
		);

		assertThat(decision.signal()).isEqualTo(StrategySignal.HOLD);
	}

	@Test
	void switchesWhenAnotherTickerIsAtLeastFivePercentagePointsCheaper() {
		StrategyDecision decision = evaluator.evaluate(
				List.of(
						result("NVDA", "-0.1400"),
						result("AMZN", "-0.2100")
				),
				Optional.of("NVDA"),
				config
		);

		assertThat(decision.signal()).isEqualTo(StrategySignal.SWITCH);
		assertThat(decision.sellTicker()).isEqualTo("NVDA");
		assertThat(decision.buyTicker()).isEqualTo("AMZN");
	}

	@Test
	void holdsCurrentPositionWhenSwitchGapIsTooSmall() {
		StrategyDecision decision = evaluator.evaluate(
				List.of(
						result("NVDA", "-0.1400"),
						result("AMZN", "-0.1700")
				),
				Optional.of("NVDA"),
				config
		);

		assertThat(decision.signal()).isEqualTo(StrategySignal.HOLD);
	}

	@Test
	void exitsWhenCurrentHoldingDiscountIsResolved() {
		StrategyDecision decision = evaluator.evaluate(
				List.of(
						result("NVDA", "0.0000"),
						result("AMZN", "-0.1000")
				),
				Optional.of("NVDA"),
				config
		);

		assertThat(decision.signal()).isEqualTo(StrategySignal.EXIT);
		assertThat(decision.sellTicker()).isEqualTo("NVDA");
	}

	@Test
	void switchHasPriorityOverExitWhenBetterCandidateMeetsSwitchRule() {
		StrategyDecision decision = evaluator.evaluate(
				List.of(
						result("NVDA", "0.0100"),
						result("AMZN", "-0.2000")
				),
				Optional.of("NVDA"),
				config
		);

		assertThat(decision.signal()).isEqualTo(StrategySignal.SWITCH);
		assertThat(decision.sellTicker()).isEqualTo("NVDA");
		assertThat(decision.buyTicker()).isEqualTo("AMZN");
	}

	@Test
	void rejectsHoldingTickerMissingFromResults() {
		assertThatThrownBy(() -> evaluator.evaluate(
				List.of(result("NVDA", "-0.1000")),
				Optional.of("AMZN"),
				config
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("current holding ticker");
	}

	private StrategyEvaluationResult result(String ticker, String peerDiscount) {
		return new StrategyEvaluationResult(
				ticker,
				new BigDecimal("40.0000"),
				new BigDecimal("50.0000"),
				new BigDecimal("0.8000"),
				new BigDecimal("1.0000"),
				new BigDecimal(peerDiscount)
		);
	}
}

