package com.mnj190.aitrading.order;

import com.mnj190.aitrading.strategy.StrategyDecision;
import com.mnj190.aitrading.strategy.StrategySignal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class OrderCandidateFactory {

	public List<OrderCandidate> create(
			StrategyDecision decision,
			BigDecimal availableCash,
			BigDecimal currentHoldingQuantity,
			BigDecimal currentHoldingMarketValue,
			String strategyVersion
	) {
		Objects.requireNonNull(decision, "decision must not be null");
		validateNonNegative(availableCash, "availableCash");
		validateNonNegative(currentHoldingMarketValue, "currentHoldingMarketValue");
		validateQuantity(currentHoldingQuantity);

		if (decision.signal() == StrategySignal.HOLD) {
			return List.of();
		}
		if (decision.signal() == StrategySignal.ENTRY) {
			return List.of(buy(decision.buyTicker(), OrderReason.ENTRY, availableCash, strategyVersion));
		}
		if (decision.signal() == StrategySignal.EXIT) {
			return List.of(sell(decision.sellTicker(), OrderReason.EXIT, currentHoldingQuantity, strategyVersion));
		}
		if (decision.signal() == StrategySignal.SWITCH) {
			BigDecimal estimatedBuyingPower = availableCash.add(currentHoldingMarketValue);
			return List.of(
					sell(decision.sellTicker(), OrderReason.SWITCH, currentHoldingQuantity, strategyVersion),
					buy(decision.buyTicker(), OrderReason.SWITCH, estimatedBuyingPower, strategyVersion)
			);
		}

		throw new IllegalArgumentException("unsupported signal: " + decision.signal());
	}

	private OrderCandidate buy(
			String ticker,
			OrderReason reason,
			BigDecimal requestedAmount,
			String strategyVersion
	) {
		return new OrderCandidate(
				ticker,
				OrderSide.BUY,
				reason,
				requestedAmount,
				null,
				OrderType.MARKET,
				strategyVersion
		);
	}

	private OrderCandidate sell(
			String ticker,
			OrderReason reason,
			BigDecimal requestedQuantity,
			String strategyVersion
	) {
		if (requestedQuantity == null || requestedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("currentHoldingQuantity must be greater than zero for sell orders");
		}
		return new OrderCandidate(
				ticker,
				OrderSide.SELL,
				reason,
				BigDecimal.ZERO,
				requestedQuantity,
				OrderType.MARKET,
				strategyVersion
		);
	}

	private void validateNonNegative(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException(name + " must be greater than or equal to zero");
		}
	}

	private void validateQuantity(BigDecimal quantity) {
		if (quantity != null && quantity.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("currentHoldingQuantity must be greater than or equal to zero");
		}
	}
}
