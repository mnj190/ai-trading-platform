package com.mnj190.aitrading.broker;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class KisTokenClient {

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisTokenClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisAccessToken issueAccessToken() {
		properties.validateCredentials();

		KisTokenResponse response = restClient.post()
				.uri("/oauth2/tokenP")
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body(KisTokenRequest.clientCredentials(properties.appKey(), properties.appSecret()))
				.retrieve()
				.body(KisTokenResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS token response body is empty");
		}

		return response.toAccessToken();
	}
}
