package com.mnj190.aitrading.strategy;

import java.util.List;
import java.util.Objects;

public record StrategyEvaluation(List<StrategyEvaluationResult> results, StrategyDecision decision) {

	public StrategyEvaluation {
		results = List.copyOf(Objects.requireNonNull(results, "results must not be null"));
		Objects.requireNonNull(decision, "decision must not be null");
	}
}

