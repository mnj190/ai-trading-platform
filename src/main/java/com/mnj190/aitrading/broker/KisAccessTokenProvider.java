package com.mnj190.aitrading.broker;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Component
public class KisAccessTokenProvider {

	private static final Duration REFRESH_MARGIN = Duration.ofMinutes(5);

	private final KisTokenClient tokenClient;
	private final Clock clock;

	private KisAccessToken cachedToken;
	private Instant expiresAt = Instant.EPOCH;

	public KisAccessTokenProvider(KisTokenClient tokenClient, Clock clock) {
		this.tokenClient = Objects.requireNonNull(tokenClient);
		this.clock = Objects.requireNonNull(clock);
	}

	public synchronized KisAccessToken getAccessToken() {
		Instant now = Instant.now(clock);
		if (cachedToken != null && now.isBefore(expiresAt.minus(REFRESH_MARGIN))) {
			return cachedToken;
		}

		KisAccessToken issuedToken = tokenClient.issueAccessToken();
		cachedToken = issuedToken;
		expiresAt = now.plusSeconds(issuedToken.expiresIn());
		return issuedToken;
	}
}
