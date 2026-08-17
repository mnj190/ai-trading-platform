package com.mnj190.aitrading.broker;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
public class KisOverseasOrderExecutionClient {

	private static final String PATH = "/uapi/overseas-stock/v1/trading/inquire-ccnl";
	private static final String REAL_TR_ID = "TTTS3035R";
	private static final String PAPER_TR_ID = "VTTS3035R";

	private final KisApiProperties properties;
	private final RestClient restClient;

	public KisOverseasOrderExecutionClient(KisApiProperties properties, RestClient.Builder restClientBuilder) {
		this.properties = Objects.requireNonNull(properties);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisOverseasOrderExecutionResponse inquireExecutions(
			KisAccessToken accessToken,
			KisOverseasOrderExecutionRequest request
	) {
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		Objects.requireNonNull(request, "request must not be null");
		properties.validateCredentials();
		properties.validateAccount();

		KisOverseasOrderExecutionResponse response = restClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(PATH)
						.queryParam("CANO", properties.accountNumber())
						.queryParam("ACNT_PRDT_CD", properties.accountProductCode())
						.queryParam("PDNO", request.productNumber())
						.queryParam("ORD_STRT_DT", request.formattedOrderStartDate())
						.queryParam("ORD_END_DT", request.formattedOrderEndDate())
						.queryParam("SLL_BUY_DVSN", request.sellBuyDivision())
						.queryParam("CCLD_NCCS_DVSN", request.executionDivision())
						.queryParam("OVRS_EXCG_CD", request.overseasExchangeCode())
						.queryParam("SORT_SQN", request.sortSequence())
						.queryParam("ORD_DT", request.orderDate())
						.queryParam("ORD_GNO_BRNO", request.orderBranchNumber())
						.queryParam("ODNO", request.orderNumber())
						.queryParam("CTX_AREA_NK200", request.contextAreaNk200())
						.queryParam("CTX_AREA_FK200", request.contextAreaFk200())
						.build())
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken.authorizationHeaderValue())
				.header("appkey", properties.appKey())
				.header("appsecret", properties.appSecret())
				.header("tr_id", trId())
				.header("tr_cont", request.transactionContinuation())
				.retrieve()
				.body(KisOverseasOrderExecutionResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS overseas order execution response body is empty");
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
