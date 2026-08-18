package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisApiPropertiesTests {

	@Test
	void rejectsBlankBaseUrl() {
		assertThatThrownBy(() -> new KisApiProperties("", "app-key", "app-secret", "12345678", "01", true))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("base-url");
	}

	@Test
	void rejectsBlankCredentialsWhenValidatingCredentials() {
		KisApiProperties properties = new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"",
				"",
				"12345678",
				"01",
				true
		);

		assertThatThrownBy(properties::validateCredentials)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("app-key");
	}

	@Test
	void rejectsBlankAccountWhenValidatingAccount() {
		KisApiProperties properties = new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"app-key",
				"app-secret",
				"",
				"01",
				true
		);

		assertThatThrownBy(properties::validateAccount)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("account-number");
	}

	@Test
	void rejectsPaperTradingTrueWithRealDomain() {
		assertThatThrownBy(() -> new KisApiProperties(
				"https://openapi.koreainvestment.com:9443",
				"app-key",
				"app-secret",
				"12345678",
				"01",
				true
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("paper-trading")
				.hasMessageContaining("base-url");
	}

	@Test
	void rejectsPaperTradingFalseWithPaperDomain() {
		assertThatThrownBy(() -> new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"app-key",
				"app-secret",
				"12345678",
				"01",
				false
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("paper-trading")
				.hasMessageContaining("base-url");
	}

	@Test
	void acceptsMatchingPaperDomainAndFlag() {
		KisApiProperties properties = new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"app-key",
				"app-secret",
				"12345678",
				"01",
				true
		);

		assertThat(properties.paperTrading()).isTrue();
	}

	@Test
	void acceptsMatchingRealDomainAndFlag() {
		KisApiProperties properties = new KisApiProperties(
				"https://openapi.koreainvestment.com:9443",
				"app-key",
				"app-secret",
				"12345678",
				"01",
				false
		);

		assertThat(properties.paperTrading()).isFalse();
	}
}
