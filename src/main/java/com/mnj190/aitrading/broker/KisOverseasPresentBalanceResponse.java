package com.mnj190.aitrading.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisOverseasPresentBalanceResponse(
		@JsonProperty("rt_cd")
		String returnCode,
		@JsonProperty("msg_cd")
		String messageCode,
		@JsonProperty("msg1")
		String message,
		Object output1,
		Object output2,
		Object output3
) {
}
