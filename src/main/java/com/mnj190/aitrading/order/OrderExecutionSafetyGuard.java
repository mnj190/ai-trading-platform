package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisApiProperties;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class OrderExecutionSafetyGuard {

	private final KisApiProperties kisApiProperties;
	private final TradingExecutionProperties executionProperties;

	public OrderExecutionSafetyGuard(
			KisApiProperties kisApiProperties,
			TradingExecutionProperties executionProperties
	) {
		this.kisApiProperties = Objects.requireNonNull(kisApiProperties);
		this.executionProperties = Objects.requireNonNull(executionProperties);
	}

	public void validate(OrderHistory order, OrderSubmissionCommand command) {
		Objects.requireNonNull(order, "order must not be null");
		Objects.requireNonNull(command, "command must not be null");

		if (!executionProperties.enabled()) {
			throw new IllegalStateException("trading execution is disabled");
		}
		if (!kisApiProperties.paperTrading() && !executionProperties.allowRealTrading()) {
			throw new IllegalStateException("real trading execution is not explicitly allowed");
		}
	}
}
