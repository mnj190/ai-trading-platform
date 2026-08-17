package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class StrategyEvaluationService {

	private final PeerAverageNormalizedPerCalculator peerAverageNormalizedPerCalculator;
	private final PeerDiscountCalculator peerDiscountCalculator;
	private final StrategyDecisionEvaluator strategyDecisionEvaluator;

	public StrategyEvaluationService() {
		this(
				new PeerAverageNormalizedPerCalculator(),
				new PeerDiscountCalculator(),
				new StrategyDecisionEvaluator()
		);
	}

	StrategyEvaluationService(
			PeerAverageNormalizedPerCalculator peerAverageNormalizedPerCalculator,
			PeerDiscountCalculator peerDiscountCalculator,
			StrategyDecisionEvaluator strategyDecisionEvaluator
	) {
		this.peerAverageNormalizedPerCalculator = Objects.requireNonNull(peerAverageNormalizedPerCalculator);
		this.peerDiscountCalculator = Objects.requireNonNull(peerDiscountCalculator);
		this.strategyDecisionEvaluator = Objects.requireNonNull(strategyDecisionEvaluator);
	}

	public StrategyEvaluation evaluate(
			List<StrategyValuationInput> valuationInputs,
			Optional<String> currentHoldingTicker,
			StrategyRuleConfig config
	) {
		Objects.requireNonNull(valuationInputs, "valuationInputs must not be null");
		Objects.requireNonNull(currentHoldingTicker, "currentHoldingTicker must not be null");
		Objects.requireNonNull(config, "config must not be null");

		BigDecimal peerAverageNormalizedPer = peerAverageNormalizedPerCalculator.calculate(valuationInputs.stream()
				.map(StrategyValuationInput::toNormalizedPerInput)
				.toList());

		List<StrategyEvaluationResult> results = valuationInputs.stream()
				.map(input -> evaluateOne(input, peerAverageNormalizedPer))
				.toList();

		StrategyDecision decision = strategyDecisionEvaluator.evaluate(results, currentHoldingTicker, config);

		return new StrategyEvaluation(results, decision);
	}

	private StrategyEvaluationResult evaluateOne(
			StrategyValuationInput input,
			BigDecimal peerAverageNormalizedPer
	) {
		BigDecimal normalizedPer = input.normalizedPer();
		BigDecimal peerDiscount = peerDiscountCalculator.calculate(normalizedPer, peerAverageNormalizedPer);

		return new StrategyEvaluationResult(
				input.ticker(),
				input.currentPer(),
				input.fiveYearAveragePer(),
				normalizedPer,
				peerAverageNormalizedPer,
				peerDiscount
		);
	}
}
