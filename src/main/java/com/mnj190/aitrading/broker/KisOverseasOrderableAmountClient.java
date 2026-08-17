package com.mnj190.aitrading.broker;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class KisOverseasOrderableAmountClient {

	private static final String PATH = "/uapi/overseas-stock/v1/trading/inquire-psamount";
	private static final String REAL_TR_ID = "TTTS3007R";
	private static final String PAPER_TR_ID = "VTTS3007R";

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisOverseasOrderableAmountClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisOverseasOrderableAmountResponse inquireOrderableAmount(
			KisAccessToken accessToken,
			KisOverseasOrderableAmountRequest request
	) {
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		Objects.requireNonNull(request, "request must not be null");
		properties.validateCredentials();
		properties.validateAccount();

		KisOverseasOrderableAmountResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PATH)
						.queryParam("CANO", properties.accountNumber())
						.queryParam("ACNT_PRDT_CD", properties.accountProductCode())
						.queryParam("OVRS_EXCG_CD", request.overseasExchangeCode())
						.queryParam("OVRS_ORD_UNPR", request.overseasOrderUnitPrice())
						.queryParam("ITEM_CD", request.itemCode())
						.build())
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken.authorizationHeaderValue())
				.header("appkey", properties.appKey())
				.header("appsecret", properties.appSecret())
				.header("tr_id", trId())
				.header("tr_cont", request.transactionContinuation())
				.retrieve()
				.body(KisOverseasOrderableAmountResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS overseas orderable amount response body is empty");
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
