package com.mnj190.aitrading.order;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trading.execution")
public record TradingExecutionProperties(
		boolean enabled,
		boolean allowRealTrading
) {
}
