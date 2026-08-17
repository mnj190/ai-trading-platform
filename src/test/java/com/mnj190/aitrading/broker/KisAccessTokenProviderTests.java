package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KisAccessTokenProviderTests {

	@TempDir
	Path tempDir;

	@Test
	void reusesTokenUntilRefreshMargin() {
		KisTokenClient tokenClient = mock(KisTokenClient.class);
		KisAccessToken issuedToken = new KisAccessToken(
				"Bearer",
				"token-value",
				86400,
				"2026-08-18 22:00:00"
		);
		when(tokenClient.issueAccessToken()).thenReturn(issuedToken);
		KisAccessTokenProvider provider = new KisAccessTokenProvider(
				properties(),
				tokenClient,
				Clock.fixed(Instant.parse("2026-08-17T14:40:00Z"), ZoneOffset.UTC),
				tempDir.resolve("kis-token-cache.properties")
		);

		KisAccessToken first = provider.getAccessToken();
		KisAccessToken second = provider.getAccessToken();

		assertThat(first).isSameAs(issuedToken);
		assertThat(second).isSameAs(issuedToken);
		verify(tokenClient, times(1)).issueAccessToken();
	}

	@Test
	void reusesCachedFileTokenAcrossProviderInstances() {
		Path tokenCachePath = tempDir.resolve("kis-token-cache.properties");
		KisTokenClient tokenClient = mock(KisTokenClient.class);
		KisAccessToken issuedToken = new KisAccessToken(
				"Bearer",
				"token-value",
				86400,
				"2026-08-18 22:00:00"
		);
		when(tokenClient.issueAccessToken()).thenReturn(issuedToken);
		Clock clock = Clock.fixed(Instant.parse("2026-08-17T14:40:00Z"), ZoneOffset.UTC);

		new KisAccessTokenProvider(
				properties(),
				tokenClient,
				clock,
				tokenCachePath
		).getAccessToken();
		KisAccessTokenProvider nextProvider = new KisAccessTokenProvider(
				properties(),
				tokenClient,
				clock,
				tokenCachePath
		);

		KisAccessToken cached = nextProvider.getAccessToken();

		assertThat(cached.accessToken()).isEqualTo("token-value");
		verify(tokenClient, times(1)).issueAccessToken();
	}

	private KisApiProperties properties() {
		return new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"test-key",
				"test-secret",
				"12345678",
				"01",
				true
		);
	}
}
