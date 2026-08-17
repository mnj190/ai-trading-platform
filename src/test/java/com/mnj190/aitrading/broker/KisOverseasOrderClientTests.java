package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.order.OrderSide;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisOverseasOrderClientTests {

	@Test
	void placesPaperUsBuyOrderWithHashKeyAndRequiredHeaders() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisApiProperties properties = properties(true);
		KisHashKeyClient hashKeyClient = new KisHashKeyClient(properties, builder);
		KisOverseasOrderClient client = new KisOverseasOrderClient(properties, hashKeyClient, builder);

		String expectedBody = """
				{
				  "CANO": "12345678",
				  "ACNT_PRDT_CD": "01",
				  "OVRS_EXCG_CD": "NASD",
				  "PDNO": "NVDA",
				  "ORD_DVSN": "00",
				  "ORD_QTY": "1",
				  "OVRS_ORD_UNPR": "180.12",
				  "ORD_SVR_DVSN_CD": "0"
				}
				""";

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/hashkey"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json(expectedBody))
				.andRespond(withSuccess("""
						{
						  "HASH": "hash-key-value"
						}
						""", MediaType.APPLICATION_JSON));

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/order"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-value"))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header("tr_id", "VTTT1002U"))
				.andExpect(header("custtype", "P"))
				.andExpect(header("hashkey", "hash-key-value"))
				.andExpect(content().json(expectedBody))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output": {
						    "ODNO": "0000000001"
						  }
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasOrderResponse response = client.placeOrder(
				token(),
				OrderSide.BUY,
				KisOverseasOrderRequest.usLimitOrder("NVDA", BigDecimal.ONE, new BigDecimal("180.123"))
		);

		assertThat(response.returnCode()).isEqualTo("0");
		assertThat(response.message()).isEqualTo("정상처리 되었습니다.");
		server.verify();
	}

	@Test
	void usesRealUsSellTrIdWhenPaperTradingIsFalse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisApiProperties properties = properties(false);
		KisHashKeyClient hashKeyClient = new KisHashKeyClient(properties, builder);
		KisOverseasOrderClient client = new KisOverseasOrderClient(properties, hashKeyClient, builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/hashkey"))
				.andRespond(withSuccess("""
						{
						  "HASH": "hash-key-value"
						}
						""", MediaType.APPLICATION_JSON));

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/order"))
				.andExpect(header("tr_id", "TTTT1006U"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output": {}
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasOrderResponse response = client.placeOrder(
				token(),
				OrderSide.SELL,
				KisOverseasOrderRequest.usLimitOrder("NVDA", BigDecimal.ONE, new BigDecimal("180.12"))
		);

		assertThat(response.returnCode()).isEqualTo("0");
		server.verify();
	}

	private KisAccessToken token() {
		return new KisAccessToken(
				"Bearer",
				"token-value",
				86400,
				"2026-08-18 22:00:00"
		);
	}

	private KisApiProperties properties(boolean paperTrading) {
		return new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"test-key",
				"test-secret",
				"12345678",
				"01",
				paperTrading
		);
	}
}
