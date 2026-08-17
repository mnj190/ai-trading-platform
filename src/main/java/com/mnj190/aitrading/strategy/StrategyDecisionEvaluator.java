package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.Objects;

public class StrategyDecisionEvaluator {

	public StrategyDecision evaluate(
			StrategyStage currentStage,
			BigDecimal peerDiscount,
			StrategyRuleConfig config
	) {
		Objects.requireNonNull(currentStage, "currentStage must not be null");
		Objects.requireNonNull(config, "config must not be null");
		validatePeerDiscount(peerDiscount);

		if (currentStage != StrategyStage.NONE && peerDiscount.compareTo(config.sellThreshold()) >= 0) {
			return StrategyDecision.sellAll();
		}

		return switch (currentStage) {
			case NONE -> buyOrHold(peerDiscount, config.buy1Threshold(), StrategyStage.BUY1, currentStage);
			case BUY1 -> buyOrHold(peerDiscount, config.buy2Threshold(), StrategyStage.BUY2, currentStage);
			case BUY2 -> buyOrHold(peerDiscount, config.buy3Threshold(), StrategyStage.BUY3, currentStage);
			case BUY3 -> StrategyDecision.hold(currentStage);
		};
	}

	private StrategyDecision buyOrHold(
			BigDecimal peerDiscount,
			BigDecimal threshold,
			StrategyStage nextStage,
			StrategyStage currentStage
	) {
		if (peerDiscount.compareTo(threshold) <= 0) {
			return StrategyDecision.buy(nextStage);
		}
		return StrategyDecision.hold(currentStage);
	}

	private void validatePeerDiscount(BigDecimal peerDiscount) {
		if (peerDiscount == null) {
			throw new IllegalArgumentException("peerDiscount must not be null");
		}
	}
}

