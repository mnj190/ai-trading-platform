package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StrategyDecisionEvaluator {

	public StrategyDecision evaluate(
			List<StrategyEvaluationResult> results,
			Optional<String> currentHoldingTicker,
			StrategyRuleConfig config
	) {
		Objects.requireNonNull(results, "results must not be null");
		Objects.requireNonNull(currentHoldingTicker, "currentHoldingTicker must not be null");
		Objects.requireNonNull(config, "config must not be null");

		StrategyEvaluationResult bestCandidate = results.stream()
				.min(Comparator.comparing(StrategyEvaluationResult::peerDiscount))
				.orElseThrow(() -> new IllegalArgumentException("results must not be empty"));

		if (currentHoldingTicker.isEmpty()) {
			if (bestCandidate.peerDiscount().compareTo(config.entryThreshold()) <= 0) {
				return StrategyDecision.entry(bestCandidate.ticker());
			}
			return StrategyDecision.hold();
		}

		String holdingTicker = currentHoldingTicker.get();
		StrategyEvaluationResult currentHolding = results.stream()
				.filter(result -> result.ticker().equals(holdingTicker))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("current holding ticker is not in evaluation results: "
						+ holdingTicker));

		if (isSwitchCandidate(bestCandidate, currentHolding, config)) {
			return StrategyDecision.switchTo(currentHolding.ticker(), bestCandidate.ticker());
		}

		if (currentHolding.peerDiscount().compareTo(config.exitThreshold()) >= 0) {
			return StrategyDecision.exit(currentHolding.ticker());
		}

		return StrategyDecision.hold();
	}

	private boolean isSwitchCandidate(
			StrategyEvaluationResult bestCandidate,
			StrategyEvaluationResult currentHolding,
			StrategyRuleConfig config
	) {
		if (bestCandidate.ticker().equals(currentHolding.ticker())) {
			return false;
		}
		if (bestCandidate.peerDiscount().compareTo(config.entryThreshold()) > 0) {
			return false;
		}

		BigDecimal switchTarget = currentHolding.peerDiscount().subtract(config.switchThreshold());
		return bestCandidate.peerDiscount().compareTo(switchTarget) <= 0;
	}
}

