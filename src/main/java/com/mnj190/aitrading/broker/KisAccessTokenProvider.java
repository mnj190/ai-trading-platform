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
		Properties cacheProperties = loadCacheFile();
		String prefix = cacheKey() + ".";
		String accessToken = cacheProperties.getProperty(prefix + "accessToken");
		if (accessToken == null) {
			return Optional.empty();
		}
		CachedAccessToken cache = new CachedAccessToken(
				cacheKey(),
				new KisAccessToken(
						cacheProperties.getProperty(prefix + "tokenType", ""),
						accessToken,
						Long.parseLong(cacheProperties.getProperty(prefix + "expiresIn", "0")),
						cacheProperties.getProperty(prefix + "accessTokenExpiredAt", "")
				),
				Instant.parse(cacheProperties.getProperty(prefix + "expiresAt"))
		);
		if (!now.isBefore(cache.expiresAt().minus(REFRESH_MARGIN))) {
			return Optional.empty();
		}
		return Optional.of(cache);
	}

	private void writeCachedFileToken(CachedAccessToken cache) {
		try {
			Path parent = tokenCachePath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Properties cacheProperties = loadCacheFile();
			String prefix = cache.cacheKey() + ".";
			cacheProperties.setProperty(prefix + "tokenType", cache.token().tokenType());
			cacheProperties.setProperty(prefix + "accessToken", cache.token().accessToken());
			cacheProperties.setProperty(prefix + "expiresIn", Long.toString(cache.token().expiresIn()));
			cacheProperties.setProperty(prefix + "accessTokenExpiredAt", cache.token().accessTokenExpiredAt());
			cacheProperties.setProperty(prefix + "expiresAt", cache.expiresAt().toString());
			try (OutputStream outputStream = Files.newOutputStream(tokenCachePath)) {
				cacheProperties.store(outputStream, "KIS access token cache. Do not commit this file. Keyed per base-url/app-key/account/paper-trading so paper and real tokens coexist.");
			}
		}
		catch (IOException ex) {
			throw new IllegalStateException("failed to write KIS access token cache", ex);
		}
	}

	private Properties loadCacheFile() {
		Properties cacheProperties = new Properties();
		if (!Files.exists(tokenCachePath)) {
			return cacheProperties;
		}
		try (InputStream inputStream = Files.newInputStream(tokenCachePath)) {
			cacheProperties.load(inputStream);
		}
		catch (IOException ignored) {
			return new Properties();
		}
		return cacheProperties;
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
