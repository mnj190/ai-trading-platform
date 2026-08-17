package com.mnj190.aitrading.strategy;

import com.mnj190.aitrading.order.OrderHistory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record DailyEvaluationReport(
		StrategyEvaluation evaluation,
		Optional<String> currentHoldingTicker,
		List<OrderHistory> requestedOrders
) {

	public DailyEvaluationReport {
		Objects.requireNonNull(evaluation, "evaluation must not be null");
		currentHoldingTicker = Objects.requireNonNull(currentHoldingTicker, "currentHoldingTicker must not be null");
		requestedOrders = List.copyOf(Objects.requireNonNull(requestedOrders, "requestedOrders must not be null"));
	}
}
