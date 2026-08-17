package com.mnj190.aitrading.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "trading.execution")
public record TradingExecutionProperties(
		boolean enabled,
		boolean allowRealTrading,
		BigDecimal maxOrderNotionalAmount
) {

	public TradingExecutionProperties {
		if (maxOrderNotionalAmount == null) {
			maxOrderNotionalAmount = new BigDecimal("500.0000");
		}
		if (maxOrderNotionalAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("trading.execution.max-order-notional-amount must be greater than zero");
		}
	}
}
