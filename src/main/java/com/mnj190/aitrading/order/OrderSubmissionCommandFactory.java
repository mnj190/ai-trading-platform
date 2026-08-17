package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisAccessToken;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class OrderSubmissionCommandFactory {

	private final WholeShareOrderSizer wholeShareOrderSizer;

	public OrderSubmissionCommandFactory(WholeShareOrderSizer wholeShareOrderSizer) {
		this.wholeShareOrderSizer = Objects.requireNonNull(wholeShareOrderSizer);
	}

	public OrderSubmissionCommand create(
			OrderHistory order,
			KisAccessToken accessToken,
			BigDecimal limitPrice
	) {
		Objects.requireNonNull(order, "order must not be null");
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		requirePositive(limitPrice, "limitPrice");

		if (order.getStatus() != OrderStatus.REQUESTED) {
			throw new IllegalStateException("only REQUESTED orders can be converted to submission commands");
		}
		BigDecimal orderQuantity = resolveQuantity(order, limitPrice);
		if (orderQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalStateException("calculated order quantity must be greater than zero");
		}
		return new OrderSubmissionCommand(order.getId(), accessToken, orderQuantity, limitPrice);
	}

	private BigDecimal resolveQuantity(OrderHistory order, BigDecimal limitPrice) {
		if (order.getSide() == OrderSide.BUY) {
			return wholeShareOrderSizer.buyQuantity(order.getRequestedAmount(), limitPrice);
		}
		if (order.getRequestedQuantity() == null) {
			throw new IllegalStateException("SELL order must have requestedQuantity");
		}
		return order.getRequestedQuantity();
	}

	private static void requirePositive(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
	}
}
