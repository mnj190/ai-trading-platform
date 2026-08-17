package com.mnj190.aitrading.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisOverseasPriceResponse(
		@JsonProperty("rt_cd")
		String returnCode,
		@JsonProperty("msg_cd")
		String messageCode,
		@JsonProperty("msg1")
		String message,
		Object output
) {
}
