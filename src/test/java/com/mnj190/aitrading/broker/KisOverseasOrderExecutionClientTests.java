package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisOverseasOrderExecutionClientTests {

	@Test
	void inquiresPaperOverseasOrderExecutionsWithRequiredHeadersAndParams() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasOrderExecutionClient client = new KisOverseasOrderExecutionClient(properties(true), builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/inquire-ccnl?CANO=12345678&ACNT_PRDT_CD=01&PDNO=&ORD_STRT_DT=20260817&ORD_END_DT=20260817&SLL_BUY_DVSN=00&CCLD_NCCS_DVSN=00&OVRS_EXCG_CD=&SORT_SQN=DS&ORD_DT=&ORD_GNO_BRNO=&ODNO=&CTX_AREA_NK200=&CTX_AREA_FK200="))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-value"))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header("tr_id", "VTTS3035R"))
				.andExpect(header("tr_cont", ""))
				.andExpect(queryParam("CANO", "12345678"))
				.andExpect(queryParam("ACNT_PRDT_CD", "01"))
				.andExpect(queryParam("ORD_STRT_DT", "20260817"))
				.andExpect(queryParam("ORD_END_DT", "20260817"))
				.andExpect(queryParam("SLL_BUY_DVSN", "00"))
				.andExpect(queryParam("CCLD_NCCS_DVSN", "00"))
				.andExpect(queryParam("SORT_SQN", "DS"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output": [
						    {
						      "odno": "0000000001",
						      "pdno": "NVDA",
						      "sll_buy_dvsn_cd": "02",
						      "ft_ccld_qty": "1",
						      "ft_ccld_unpr3": "180.1200"
						    }
						  ],
						  "ctx_area_fk200": "",
						  "ctx_area_nk200": ""
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasOrderExecutionResponse response = client.inquireExecutions(
				token(),
				KisOverseasOrderExecutionRequest.allForDate(LocalDate.of(2026, 8, 17))
		);

		assertThat(response.returnCode()).isEqualTo("0");
		assertThat(response.output()).isInstanceOf(List.class);
		List<?> executions = (List<?>) response.output();
		assertThat(executions).hasSize(1);
		@SuppressWarnings("unchecked")
		Map<String, Object> execution = (Map<String, Object>) executions.getFirst();
		assertThat(execution)
				.containsEntry("odno", "0000000001")
				.containsEntry("pdno", "NVDA");
		server.verify();
	}

	@Test
	void usesRealTrIdWhenPaperTradingIsFalse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasOrderExecutionClient client = new KisOverseasOrderExecutionClient(properties(false), builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/inquire-ccnl?CANO=12345678&ACNT_PRDT_CD=01&PDNO=&ORD_STRT_DT=20260817&ORD_END_DT=20260817&SLL_BUY_DVSN=00&CCLD_NCCS_DVSN=00&OVRS_EXCG_CD=&SORT_SQN=DS&ORD_DT=&ORD_GNO_BRNO=&ODNO=&CTX_AREA_NK200=&CTX_AREA_FK200="))
				.andExpect(header("tr_id", "TTTS3035R"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output": [],
						  "ctx_area_fk200": "",
						  "ctx_area_nk200": ""
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasOrderExecutionResponse response = client.inquireExecutions(
				token(),
				KisOverseasOrderExecutionRequest.allForDate(LocalDate.of(2026, 8, 17))
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
