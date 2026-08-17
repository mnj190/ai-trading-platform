package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrategyDecisionEvaluatorTests {

	private final StrategyDecisionEvaluator evaluator = new StrategyDecisionEvaluator();
	private final StrategyRuleConfig config = StrategyRuleConfig.peMeanReversionV1();

	@Test
	void buysBuy1FromNoneWhenDiscountReachesBuy1Threshold() {
		StrategyDecision decision = evaluate(StrategyStage.NONE, "-0.1500");

		assertThat(decision.signal()).isEqualTo(StrategySignal.BUY);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.BUY1);
	}

	@Test
	void holdsNoneWhenDiscountDoesNotReachBuy1Threshold() {
		StrategyDecision decision = evaluate(StrategyStage.NONE, "-0.1499");

		assertThat(decision.signal()).isEqualTo(StrategySignal.HOLD);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.NONE);
	}

	@Test
	void doesNotRepeatBuy1Stage() {
		StrategyDecision decision = evaluate(StrategyStage.BUY1, "-0.1900");

		assertThat(decision.signal()).isEqualTo(StrategySignal.HOLD);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.BUY1);
	}

	@Test
	void buysBuy2FromBuy1WhenDiscountReachesBuy2Threshold() {
		StrategyDecision decision = evaluate(StrategyStage.BUY1, "-0.2000");

		assertThat(decision.signal()).isEqualTo(StrategySignal.BUY);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.BUY2);
	}

	@Test
	void buysBuy3FromBuy2WhenDiscountReachesBuy3Threshold() {
		StrategyDecision decision = evaluate(StrategyStage.BUY2, "-0.2500");

		assertThat(decision.signal()).isEqualTo(StrategySignal.BUY);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.BUY3);
	}

	@Test
	void holdsBuy3WhenDiscountFallsFurther() {
		StrategyDecision decision = evaluate(StrategyStage.BUY3, "-0.3000");

		assertThat(decision.signal()).isEqualTo(StrategySignal.HOLD);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.BUY3);
	}

	@Test
	void sellsHeldPositionWhenDiscountReturnsToPeerAverage() {
		StrategyDecision decision = evaluate(StrategyStage.BUY2, "0.0000");

		assertThat(decision.signal()).isEqualTo(StrategySignal.SELL);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.NONE);
	}

	@Test
	void holdsNoneWhenNoPositionMeetsSellCondition() {
		StrategyDecision decision = evaluate(StrategyStage.NONE, "0.0000");

		assertThat(decision.signal()).isEqualTo(StrategySignal.HOLD);
		assertThat(decision.nextStage()).isEqualTo(StrategyStage.NONE);
	}

	@Test
	void rejectsNullPeerDiscount() {
		assertThatThrownBy(() -> evaluator.evaluate(StrategyStage.NONE, null, config))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("peerDiscount");
	}

	private StrategyDecision evaluate(StrategyStage currentStage, String peerDiscount) {
		return evaluator.evaluate(currentStage, new BigDecimal(peerDiscount), config);
	}
}

