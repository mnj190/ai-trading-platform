package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KisAccessTokenProviderTests {

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
				tokenClient,
				Clock.fixed(Instant.parse("2026-08-17T14:40:00Z"), ZoneOffset.UTC)
		);

		KisAccessToken first = provider.getAccessToken();
		KisAccessToken second = provider.getAccessToken();

		assertThat(first).isSameAs(issuedToken);
		assertThat(second).isSameAs(issuedToken);
		verify(tokenClient, times(1)).issueAccessToken();
	}
}
