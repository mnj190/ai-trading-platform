package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisOverseasOrderableAmountClientTests {

	@Test
	void inquiresPaperOrderableAmountWithRequiredHeadersAndParams() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasOrderableAmountClient client = new KisOverseasOrderableAmountClient(properties(true), builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/inquire-psamount?CANO=12345678&ACNT_PRDT_CD=01&OVRS_EXCG_CD=NASD&OVRS_ORD_UNPR=225.13&ITEM_CD=AAPL"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-value"))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header("tr_id", "VTTS3007R"))
				.andExpect(header("tr_cont", ""))
				.andExpect(queryParam("CANO", "12345678"))
				.andExpect(queryParam("ACNT_PRDT_CD", "01"))
				.andExpect(queryParam("OVRS_EXCG_CD", "NASD"))
				.andExpect(queryParam("OVRS_ORD_UNPR", "225.13"))
				.andExpect(queryParam("ITEM_CD", "AAPL"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output": {
						    "max_ord_psbl_qty": "4"
						  }
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasOrderableAmountResponse response = client.inquireOrderableAmount(
				token(),
				KisOverseasOrderableAmountRequest.nasdaq("AAPL", new BigDecimal("225.126"))
		);

		assertThat(response.returnCode()).isEqualTo("0");
		assertThat(response.output()).isInstanceOf(Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> output = (Map<String, Object>) response.output();
		assertThat(output).containsEntry("max_ord_psbl_qty", "4");
		server.verify();
	}

	@Test
	void usesRealTrIdWhenPaperTradingIsFalse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasOrderableAmountClient client = new KisOverseasOrderableAmountClient(properties(false), builder);

		server.expect(once(), requestTo("https://openapi.koreainvestment.com:9443/uapi/overseas-stock/v1/trading/inquire-psamount?CANO=12345678&ACNT_PRDT_CD=01&OVRS_EXCG_CD=NASD&OVRS_ORD_UNPR=225.13&ITEM_CD=AAPL"))
				.andExpect(header("tr_id", "TTTS3007R"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output": {}
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasOrderableAmountResponse response = client.inquireOrderableAmount(
				token(),
				KisOverseasOrderableAmountRequest.nasdaq("AAPL", new BigDecimal("225.126"))
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
				paperTrading
						? "https://openapivts.koreainvestment.com:29443"
						: "https://openapi.koreainvestment.com:9443",
				"test-key",
				"test-secret",
				"12345678",
				"01",
				paperTrading
		);
	}
}
