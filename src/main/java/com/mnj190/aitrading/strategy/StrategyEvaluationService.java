package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StrategyEvaluationService {

	private final PeerAveragePerCalculator peerAveragePerCalculator;
	private final PeerDiscountCalculator peerDiscountCalculator;
	private final StrategyDecisionEvaluator strategyDecisionEvaluator;

	public StrategyEvaluationService() {
		this(
				new PeerAveragePerCalculator(),
				new PeerDiscountCalculator(),
				new StrategyDecisionEvaluator()
		);
	}

	StrategyEvaluationService(
			PeerAveragePerCalculator peerAveragePerCalculator,
			PeerDiscountCalculator peerDiscountCalculator,
			StrategyDecisionEvaluator strategyDecisionEvaluator
	) {
		this.peerAveragePerCalculator = Objects.requireNonNull(peerAveragePerCalculator);
		this.peerDiscountCalculator = Objects.requireNonNull(peerDiscountCalculator);
		this.strategyDecisionEvaluator = Objects.requireNonNull(strategyDecisionEvaluator);
	}

	public List<StrategyEvaluationResult> evaluate(
			List<PeerPerInput> peerInputs,
			Map<String, StrategyStage> currentStages,
			StrategyRuleConfig config
	) {
		Objects.requireNonNull(peerInputs, "peerInputs must not be null");
		Objects.requireNonNull(currentStages, "currentStages must not be null");
		Objects.requireNonNull(config, "config must not be null");

		BigDecimal peerAveragePer = peerAveragePerCalculator.calculate(peerInputs);

		return peerInputs.stream()
				.map(input -> evaluateOne(input, peerAveragePer, currentStages, config))
				.toList();
	}

	private StrategyEvaluationResult evaluateOne(
			PeerPerInput input,
			BigDecimal peerAveragePer,
			Map<String, StrategyStage> currentStages,
			StrategyRuleConfig config
	) {
		BigDecimal peerDiscount = peerDiscountCalculator.calculate(input.currentPer(), peerAveragePer);
		StrategyStage currentStage = currentStages.getOrDefault(input.ticker(), StrategyStage.NONE);
		StrategyDecision decision = strategyDecisionEvaluator.evaluate(currentStage, peerDiscount, config);

		return new StrategyEvaluationResult(
				input.ticker(),
				input.currentPer(),
				peerAveragePer,
				peerDiscount,
				currentStage,
				decision
		);
	}
}

