package com.mnj190.aitrading.broker;

import java.util.Objects;

public record KisAccessToken(
		String tokenType,
		String accessToken,
		long expiresIn,
		String accessTokenExpiredAt
) {

	public KisAccessToken {
		if (accessToken == null || accessToken.isBlank()) {
			throw new IllegalArgumentException("accessToken must not be blank");
		}
		Objects.requireNonNull(tokenType, "tokenType must not be null");
	}
}
