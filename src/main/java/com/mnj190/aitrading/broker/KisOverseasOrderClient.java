package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.order.OrderSide;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.Objects;

@Component
public class KisOverseasOrderClient {

	private static final String PATH = "/uapi/overseas-stock/v1/trading/order";
	private static final String REAL_US_BUY_TR_ID = "TTTT1002U";
	private static final String REAL_US_SELL_TR_ID = "TTTT1006U";
	private static final String PAPER_US_BUY_TR_ID = "VTTT1002U";
	private static final String PAPER_US_SELL_TR_ID = "VTTT1001U";

	private final KisApiProperties properties;
	private final KisHashKeyClient hashKeyClient;
	private final RestClient restClient;

	public KisOverseasOrderClient(
			KisApiProperties properties,
			KisHashKeyClient hashKeyClient,
			RestClient.Builder restClientBuilder
	) {
		this.properties = Objects.requireNonNull(properties);
		this.hashKeyClient = Objects.requireNonNull(hashKeyClient);
		this.restClient = Objects.requireNonNull(restClientBuilder)
				.baseUrl(properties.baseUrl())
				.build();
	}

	public KisOverseasOrderResponse placeOrder(
			KisAccessToken accessToken,
			OrderSide side,
			KisOverseasOrderRequest request
	) {
		Objects.requireNonNull(accessToken, "accessToken must not be null");
		Objects.requireNonNull(side, "side must not be null");
		Objects.requireNonNull(request, "request must not be null");
		properties.validateCredentials();
		properties.validateAccount();

		if (!request.isUsOrder()) {
			throw new IllegalArgumentException("only US overseas stock orders are supported in V1");
		}

		Map<String, String> body = request.toBody(properties);
		body.put("CTAC_TLNO", "");
		body.put("MGCO_APTM_ODNO", "");
		body.put("SLL_TYPE", side == OrderSide.SELL ? "00" : "");
		String hashKey = hashKeyClient.issueHashKey(body);
		pauseBetweenKisRequests();

		KisOverseasOrderResponse response = restClient.post()
				.uri(PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION, accessToken.authorizationHeaderValue())
				.header("appkey", properties.appKey())
				.header("appsecret", properties.appSecret())
				.header("tr_id", trId(side))
				.header("custtype", "P")
				.header("hashkey", hashKey)
				.body(body)
				.retrieve()
				.body(KisOverseasOrderResponse.class);

		if (response == null) {
			throw new IllegalStateException("KIS overseas order response body is empty");
		}
		return response;
	}

	private String trId(OrderSide side) {
		if (properties.paperTrading()) {
			if (side == OrderSide.BUY) {
				return PAPER_US_BUY_TR_ID;
			}
			return PAPER_US_SELL_TR_ID;
		}
		if (side == OrderSide.BUY) {
			return REAL_US_BUY_TR_ID;
		}
		return REAL_US_SELL_TR_ID;
	}

	private void pauseBetweenKisRequests() {
		try {
			Thread.sleep(1_200);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("interrupted while waiting for KIS request throttle", ex);
		}
	}
}
