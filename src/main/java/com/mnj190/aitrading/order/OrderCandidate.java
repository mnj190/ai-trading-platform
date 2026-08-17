package com.mnj190.aitrading.order;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderCandidate(
		String ticker,
		OrderSide side,
		OrderReason reason,
		BigDecimal requestedAmount,
		BigDecimal requestedQuantity,
		OrderType orderType,
		String strategyVersion
) {

	public OrderCandidate {
		requireTicker(ticker);
		Objects.requireNonNull(side, "side must not be null");
		Objects.requireNonNull(reason, "reason must not be null");
		Objects.requireNonNull(requestedAmount, "requestedAmount must not be null");
		Objects.requireNonNull(orderType, "orderType must not be null");
		requireStrategyVersion(strategyVersion);

		if (requestedAmount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("requestedAmount must be greater than or equal to zero");
		}
		if (requestedQuantity != null && requestedQuantity.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("requestedQuantity must be greater than or equal to zero");
		}
	}

	private static void requireTicker(String ticker) {
		if (ticker == null || ticker.isBlank()) {
			throw new IllegalArgumentException("ticker must not be blank");
		}
	}

	private static void requireStrategyVersion(String strategyVersion) {
		if (strategyVersion == null || strategyVersion.isBlank()) {
			throw new IllegalArgumentException("strategyVersion must not be blank");
		}
	}
}
