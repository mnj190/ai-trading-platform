package com.mnj190.aitrading.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

record KisHashKeyResponse(
		@JsonProperty("HASH")
		String hash
) {
}
