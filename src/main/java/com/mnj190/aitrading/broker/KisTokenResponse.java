package com.mnj190.aitrading.broker;

import com.fasterxml.jackson.annotation.JsonProperty;

record KisTokenResponse(
		@JsonProperty("token_type")
		String tokenType,
		@JsonProperty("access_token")
		String accessToken,
		@JsonProperty("expires_in")
		long expiresIn,
		@JsonProperty("access_token_token_expired")
		String accessTokenExpiredAt
) {

	KisAccessToken toAccessToken() {
		return new KisAccessToken(
				tokenType,
				accessToken,
				expiresIn,
				accessTokenExpiredAt
		);
	}
}
