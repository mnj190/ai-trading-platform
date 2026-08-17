package com.mnj190.aitrading.broker;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kis.api")
public record KisApiProperties(
		String baseUrl,
		String appKey,
		String appSecret,
		String accountNumber,
		String accountProductCode,
		boolean paperTrading
) {

	public KisApiProperties {
		if (baseUrl == null || baseUrl.isBlank()) {
			throw new IllegalArgumentException("kis.api.base-url must not be blank");
		}
	}

	public void validateCredentials() {
		if (appKey == null || appKey.isBlank()) {
			throw new IllegalStateException("kis.api.app-key must not be blank");
		}
		if (appSecret == null || appSecret.isBlank()) {
			throw new IllegalStateException("kis.api.app-secret must not be blank");
		}
	}

	public void validateAccount() {
		if (accountNumber == null || accountNumber.isBlank()) {
			throw new IllegalStateException("kis.api.account-number must not be blank");
		}
		if (accountProductCode == null || accountProductCode.isBlank()) {
			throw new IllegalStateException("kis.api.account-product-code must not be blank");
		}
	}
}
