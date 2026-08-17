package com.mnj190.aitrading.broker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

@Component
public class KisAccessTokenProvider {

	private static final Duration REFRESH_MARGIN = Duration.ofMinutes(5);

	private final KisApiProperties properties;
	private final KisTokenClient tokenClient;
	private final Clock clock;
	private final Path tokenCachePath;

	private KisAccessToken cachedToken;
	private Instant expiresAt = Instant.EPOCH;

	public KisAccessTokenProvider(
			KisApiProperties properties,
			KisTokenClient tokenClient,
			Clock clock,
			@Value("${kis.api.token-cache-path:tmp/kis-access-token-cache.properties}") Path tokenCachePath
	) {
		this.properties = Objects.requireNonNull(properties);
		this.tokenClient = Objects.requireNonNull(tokenClient);
		this.clock = Objects.requireNonNull(clock);
		this.tokenCachePath = Objects.requireNonNull(tokenCachePath);
	}

	public synchronized KisAccessToken getAccessToken() {
		Instant now = Instant.now(clock);
		if (cachedToken != null && now.isBefore(expiresAt.minus(REFRESH_MARGIN))) {
			return cachedToken;
		}
		Optional<CachedAccessToken> cachedFileToken = readCachedFileToken(now);
		if (cachedFileToken.isPresent()) {
			CachedAccessToken cache = cachedFileToken.get();
			cachedToken = cache.token();
			expiresAt = cache.expiresAt();
			return cachedToken;
		}

		KisAccessToken issuedToken = tokenClient.issueAccessToken();
		cachedToken = issuedToken;
		expiresAt = now.plusSeconds(issuedToken.expiresIn());
		writeCachedFileToken(new CachedAccessToken(cacheKey(), issuedToken, expiresAt));
		return issuedToken;
	}

	private Optional<CachedAccessToken> readCachedFileToken(Instant now) {
		if (!Files.exists(tokenCachePath)) {
			return Optional.empty();
		}
		try {
			Properties cacheProperties = new Properties();
			try (InputStream inputStream = Files.newInputStream(tokenCachePath)) {
				cacheProperties.load(inputStream);
			}
			CachedAccessToken cache = new CachedAccessToken(
					cacheProperties.getProperty("cacheKey"),
					new KisAccessToken(
							cacheProperties.getProperty("tokenType", ""),
							cacheProperties.getProperty("accessToken"),
							Long.parseLong(cacheProperties.getProperty("expiresIn", "0")),
							cacheProperties.getProperty("accessTokenExpiredAt", "")
					),
					Instant.parse(cacheProperties.getProperty("expiresAt"))
			);
			if (!cacheKey().equals(cache.cacheKey())) {
				return Optional.empty();
			}
			if (!now.isBefore(cache.expiresAt().minus(REFRESH_MARGIN))) {
				return Optional.empty();
			}
			return Optional.of(cache);
		}
		catch (IOException ignored) {
			return Optional.empty();
		}
	}

	private void writeCachedFileToken(CachedAccessToken cache) {
		try {
			Path parent = tokenCachePath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Properties cacheProperties = new Properties();
			cacheProperties.setProperty("cacheKey", cache.cacheKey());
			cacheProperties.setProperty("tokenType", cache.token().tokenType());
			cacheProperties.setProperty("accessToken", cache.token().accessToken());
			cacheProperties.setProperty("expiresIn", Long.toString(cache.token().expiresIn()));
			cacheProperties.setProperty("accessTokenExpiredAt", cache.token().accessTokenExpiredAt());
			cacheProperties.setProperty("expiresAt", cache.expiresAt().toString());
			try (OutputStream outputStream = Files.newOutputStream(tokenCachePath)) {
				cacheProperties.store(outputStream, "KIS access token cache. Do not commit this file.");
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("failed to write KIS access token cache", ex);
		}
	}

	private String cacheKey() {
		String source = properties.baseUrl()
				+ "|" + properties.appKey()
				+ "|" + properties.accountNumber()
				+ "|" + properties.accountProductCode()
				+ "|" + properties.paperTrading();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(source.getBytes(StandardCharsets.UTF_8)));
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}

	private record CachedAccessToken(
			String cacheKey,
			KisAccessToken token,
			Instant expiresAt
	) {
	}
}
