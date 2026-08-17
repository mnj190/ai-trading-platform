package com.mnj190.aitrading.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

record KisTokenRequest(
		@JsonProperty("grant_type")
		String grantType,
		String appkey,
		String appsecret
) {

	static KisTokenRequest clientCredentials(String appKey, String appSecret) {
		return new KisTokenRequest("client_credentials", appKey, appSecret);
	}
}
