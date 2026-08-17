package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisOverseasPresentBalanceClientTests {

	@Test
	void inquiresPaperPresentBalanceWithRequiredHeadersAndParams() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasPresentBalanceClient client = new KisOverseasPresentBalanceClient(properties(true), builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/inquire-present-balance?CANO=12345678&ACNT_PRDT_CD=01&WCRC_FRCR_DVSN_CD=02&NATN_CD=000&TR_MKET_CD=00&INQR_DVSN_CD=00"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-value"))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header("tr_id", "VTRP6504R"))
				.andExpect(header("tr_cont", ""))
				.andExpect(queryParam("CANO", "12345678"))
				.andExpect(queryParam("ACNT_PRDT_CD", "01"))
				.andExpect(queryParam("WCRC_FRCR_DVSN_CD", "02"))
				.andExpect(queryParam("NATN_CD", "000"))
				.andExpect(queryParam("TR_MKET_CD", "00"))
				.andExpect(queryParam("INQR_DVSN_CD", "00"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "KIOK0000",
						  "msg1": "정상처리 되었습니다.",
						  "output1": [],
						  "output2": {
						    "frcr_dncl_amt_2": "100.00"
						  },
						  "output3": {
						    "tot_asst_amt": "135000"
						  }
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasPresentBalanceResponse response = client.inquirePresentBalance(
				token(),
				KisOverseasPresentBalanceRequest.allForeignCurrency()
		);

		assertThat(response.returnCode()).isEqualTo("0");
		assertThat(response.output2()).isInstanceOf(Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> output2 = (Map<String, Object>) response.output2();
		assertThat(output2).containsEntry("frcr_dncl_amt_2", "100.00");
		server.verify();
	}

	@Test
	void usesRealTrIdWhenPaperTradingIsFalse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasPresentBalanceClient client = new KisOverseasPresentBalanceClient(properties(false), builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/inquire-present-balance?CANO=12345678&ACNT_PRDT_CD=01&WCRC_FRCR_DVSN_CD=01&NATN_CD=000&TR_MKET_CD=00&INQR_DVSN_CD=00"))
				.andExpect(header("tr_id", "CTRP6504R"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "KIOK0000",
						  "msg1": "정상처리 되었습니다.",
						  "output1": [],
						  "output2": {},
						  "output3": {}
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasPresentBalanceResponse response = client.inquirePresentBalance(
				token(),
				KisOverseasPresentBalanceRequest.allWon()
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
