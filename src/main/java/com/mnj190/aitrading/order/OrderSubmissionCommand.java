package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisAccessToken;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderSubmissionCommand(
		Long orderId,
		KisAccessToken accessToken,
		BigDecimal orderQuantity,
		BigDecimal limitPrice
) {

	public OrderSubmissionCommand {
		Objects.requireNonNull(orderId, "orderId must not be null");
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		requirePositive(orderQuantity, "orderQuantity");
		requirePositive(limitPrice, "limitPrice");
	}

	private static void requirePositive(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
	}
}
