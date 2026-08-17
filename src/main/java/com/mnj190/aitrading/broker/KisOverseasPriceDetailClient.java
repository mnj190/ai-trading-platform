package com.mnj190.aitrading.broker;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class KisOverseasPriceDetailClient {

	private static final String PATH = "/uapi/overseas-price/v1/quotations/price-detail";
	private static final String TR_ID = "HHDFS76200200";

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisOverseasPriceDetailClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisOverseasPriceDetailResponse inquireNasdaqPriceDetail(KisAccessToken accessToken, String symbol) {
		return inquirePriceDetail(accessToken, KisOverseasPriceDetailRequest.nasdaq(symbol));
	}

	public KisOverseasPriceDetailResponse inquirePriceDetail(
			KisAccessToken accessToken,
			KisOverseasPriceDetailRequest request
	) {
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		Objects.requireNonNull(request, "request must not be null");
		properties.validateCredentials();

		KisOverseasPriceDetailResponse response = restClient.get()
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
				.body(KisOverseasPriceDetailResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS overseas price detail response body is empty");
		}
		return response;
	}
}
