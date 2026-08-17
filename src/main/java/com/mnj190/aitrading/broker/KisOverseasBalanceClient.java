package com.mnj190.aitrading.broker;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class KisOverseasBalanceClient {

	private static final String PATH = "/uapi/overseas-stock/v1/trading/inquire-balance";
	private static final String REAL_TR_ID = "TTTS3012R";
	private static final String PAPER_TR_ID = "VTTS3012R";

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisOverseasBalanceClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisOverseasBalanceResponse inquireNasdaqUsdBalance(KisAccessToken accessToken) {
		return inquireBalance(accessToken, KisOverseasBalanceRequest.nasdaqUsdFirstPage());
	}

	public KisOverseasBalanceResponse inquireBalance(
			KisAccessToken accessToken,
			KisOverseasBalanceRequest request
	) {
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		Objects.requireNonNull(request, "request must not be null");
		properties.validateCredentials();
		properties.validateAccount();

		KisOverseasBalanceResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PATH)
						.queryParam("CANO", properties.accountNumber())
						.queryParam("ACNT_PRDT_CD", properties.accountProductCode())
						.queryParam("OVRS_EXCG_CD", request.overseasExchangeCode())
						.queryParam("TR_CRCY_CD", request.transactionCurrencyCode())
						.queryParam("CTX_AREA_FK200", request.contextAreaFk200())
						.queryParam("CTX_AREA_NK200", request.contextAreaNk200())
						.build())
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken.authorizationHeaderValue())
				.header("appkey", properties.appKey())
				.header("appsecret", properties.appSecret())
				.header("tr_id", trId())
				.header("tr_cont", request.transactionContinuation())
				.retrieve()
				.body(KisOverseasBalanceResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS overseas balance response body is empty");
		}
		return response;
	}

	private String trId() {
		if (properties.paperTrading()) {
			return PAPER_TR_ID;
		}
		return REAL_TR_ID;
	}
}
