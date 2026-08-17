package com.mnj190.aitrading.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Optional;

public record KisOverseasOrderResponse(
		@JsonProperty("rt_cd")
		String returnCode,
		@JsonProperty("msg_cd")
		String messageCode,
		@JsonProperty("msg1")
		String message,
		Object output
) {

	public boolean isSuccess() {
		return "0".equals(returnCode);
	}

	public Optional<String> brokerOrderId() {
		if (!(output instanceof Map<?, ?> outputMap)) {
			return Optional.empty();
		}
		Object orderNumber = outputMap.get("ODNO");
		if (orderNumber == null) {
			orderNumber = outputMap.get("odno");
		}
		if (orderNumber == null) {
			return Optional.empty();
		}
		String value = orderNumber.toString();
		if (value.isBlank()) {
			return Optional.empty();
		}
		return Optional.of(value);
	}
}
