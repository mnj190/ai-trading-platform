package com.mnj190.aitrading.broker;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class KisOverseasPresentBalanceClient {

	private static final String PATH = "/uapi/overseas-stock/v1/trading/inquire-present-balance";
	private static final String REAL_TR_ID = "CTRP6504R";
	private static final String PAPER_TR_ID = "VTRP6504R";

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisOverseasPresentBalanceClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisOverseasPresentBalanceResponse inquirePresentBalance(
			KisAccessToken accessToken,
			KisOverseasPresentBalanceRequest request
	) {
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		Objects.requireNonNull(request, "request must not be null");
		properties.validateCredentials();
		properties.validateAccount();

		KisOverseasPresentBalanceResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PATH)
						.queryParam("CANO", properties.accountNumber())
						.queryParam("ACNT_PRDT_CD", properties.accountProductCode())
						.queryParam("WCRC_FRCR_DVSN_CD", request.wonForeignCurrencyDivisionCode())
						.queryParam("NATN_CD", request.nationCode())
						.queryParam("TR_MKET_CD", request.tradingMarketCode())
						.queryParam("INQR_DVSN_CD", request.inquiryDivisionCode())
						.build())
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken.authorizationHeaderValue())
				.header("appkey", properties.appKey())
				.header("appsecret", properties.appSecret())
				.header("tr_id", trId())
				.header("tr_cont", request.transactionContinuation())
				.retrieve()
				.body(KisOverseasPresentBalanceResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS overseas present balance response body is empty");
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
