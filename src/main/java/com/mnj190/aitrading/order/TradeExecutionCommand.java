package com.mnj190.aitrading.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public record TradeExecutionCommand(
		Long orderId,
		BigDecimal cumulativeFilledQuantity,
		BigDecimal executedPrice,
		OffsetDateTime executedAt
) {

	public TradeExecutionCommand {
		Objects.requireNonNull(orderId, "orderId must not be null");
		Objects.requireNonNull(cumulativeFilledQuantity, "cumulativeFilledQuantity must not be null");
		Objects.requireNonNull(executedPrice, "executedPrice must not be null");
		Objects.requireNonNull(executedAt, "executedAt must not be null");

		if (cumulativeFilledQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("cumulativeFilledQuantity must be greater than zero");
		}
		if (executedPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("executedPrice must be greater than zero");
		}
	}
}
