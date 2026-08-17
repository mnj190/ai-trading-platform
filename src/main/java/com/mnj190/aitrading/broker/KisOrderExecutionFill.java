package com.mnj190.aitrading.broker;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public record KisOrderExecutionFill(
		String brokerOrderId,
		String ticker,
		BigDecimal orderedQuantity,
		BigDecimal cumulativeExecutedQuantity,
		BigDecimal executedPrice,
		OffsetDateTime executedAt
) {

	public KisOrderExecutionFill {
		requireNotBlank(brokerOrderId, "brokerOrderId");
		requireNotBlank(ticker, "ticker");
		Objects.requireNonNull(orderedQuantity, "orderedQuantity must not be null");
		Objects.requireNonNull(cumulativeExecutedQuantity, "cumulativeExecutedQuantity must not be null");
		Objects.requireNonNull(executedPrice, "executedPrice must not be null");
		Objects.requireNonNull(executedAt, "executedAt must not be null");

		if (orderedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("orderedQuantity must be greater than zero");
		}
		if (cumulativeExecutedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("cumulativeExecutedQuantity must be greater than zero");
		}
		if (executedPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("executedPrice must be greater than zero");
		}
	}

	private static void requireNotBlank(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
	}
}
