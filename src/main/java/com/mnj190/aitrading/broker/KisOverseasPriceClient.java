package com.mnj190.aitrading.broker;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class KisOverseasPriceClient {

	private static final String PATH = "/uapi/overseas-price/v1/quotations/price";
	private static final String TR_ID = "HHDFS00000300";

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisOverseasPriceClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisOverseasPriceResponse inquireNasdaqPrice(KisAccessToken accessToken, String symbol) {
		return inquirePrice(accessToken, KisOverseasPriceRequest.nasdaq(symbol));
	}

	public KisOverseasPriceResponse inquirePrice(
			KisAccessToken accessToken,
			KisOverseasPriceRequest request
	) {
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		Objects.requireNonNull(request, "request must not be null");
		properties.validateCredentials();

		KisOverseasPriceResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PATH)
						.queryParam("AUTH", request.auth())
						.queryParam("EXCD", request.exchangeCode())
						.queryParam("SYMB", request.symbol())
						.build())
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken.authorizationHeaderValue())
				.header("appkey", properties.appKey())
				.header("appsecret", properties.appSecret())
				.header("tr_id", TR_ID)
				.header("tr_cont", request.transactionContinuation())
				.retrieve()
				.body(KisOverseasPriceResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS overseas price response body is empty");
		}
		return response;
	}
}
