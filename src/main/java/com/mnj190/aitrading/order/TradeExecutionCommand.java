package com.mnj190.aitrading.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public record TradeExecutionCommand(
		Long orderId,
		BigDecimal executedQuantity,
		BigDecimal executedPrice,
		OffsetDateTime executedAt
) {

	public TradeExecutionCommand {
		Objects.requireNonNull(orderId, "orderId must not be null");
		Objects.requireNonNull(executedQuantity, "executedQuantity must not be null");
		Objects.requireNonNull(executedPrice, "executedPrice must not be null");
		Objects.requireNonNull(executedAt, "executedAt must not be null");

		if (executedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("executedQuantity must be greater than zero");
		}
		if (executedPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("executedPrice must be greater than zero");
		}
	}
}
