package com.mnj190.aitrading.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KisOverseasBalanceResponse(
		@JsonProperty("rt_cd")
		String returnCode,
		@JsonProperty("msg_cd")
		String messageCode,
		@JsonProperty("msg1")
		String message,
		Object output1,
		Object output2,
		@JsonProperty("ctx_area_fk200")
		String contextAreaFk200,
		@JsonProperty("ctx_area_nk200")
		String contextAreaNk200
) {
}
