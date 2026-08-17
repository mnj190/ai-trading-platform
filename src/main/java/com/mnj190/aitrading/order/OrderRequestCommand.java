package com.mnj190.aitrading.order;

import com.mnj190.aitrading.strategy.StrategyDecision;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public record OrderRequestCommand(
		StrategyDecision decision,
		BigDecimal availableCash,
		BigDecimal currentHoldingQuantity,
		BigDecimal currentHoldingMarketValue,
		String strategyVersion,
		OffsetDateTime orderedAt
) {

	public OrderRequestCommand {
		Objects.requireNonNull(decision, "decision must not be null");
		Objects.requireNonNull(availableCash, "availableCash must not be null");
		Objects.requireNonNull(currentHoldingMarketValue, "currentHoldingMarketValue must not be null");
		Objects.requireNonNull(orderedAt, "orderedAt must not be null");

		if (strategyVersion == null || strategyVersion.isBlank()) {
			throw new IllegalArgumentException("strategyVersion must not be blank");
		}
	}
}
