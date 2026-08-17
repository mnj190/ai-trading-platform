package com.mnj190.aitrading.broker;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

@Component
public class KisHashKeyClient {

	private static final String PATH = "/uapi/hashkey";

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisHashKeyClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public String issueHashKey(Map<String, String> body) {
		Objects.requireNonNull(body, "body must not be null");
		properties.validateCredentials();

		KisHashKeyResponse response = restClient.post()
				.uri(PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header("appkey", properties.appKey())
				.header("appsecret", properties.appSecret())
				.body(body)
				.retrieve()
				.body(KisHashKeyResponse.class);

		if (response == null || response.hash() == null || response.hash().isBlank()) {
			throw new IllegalStateException("KIS hashkey response body is empty");
		}
		return response.hash();
	}
}
