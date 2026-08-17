package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisOverseasBalanceClientTests {

	@Test
	void inquiresPaperOverseasBalanceWithRequiredHeadersAndParams() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasBalanceClient client = new KisOverseasBalanceClient(
				new KisApiProperties(
						"https://openapivts.koreainvestment.com:29443",
						"test-key",
						"test-secret",
						"12345678",
						"01",
						true
				),
				builder
		);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/inquire-balance?CANO=12345678&ACNT_PRDT_CD=01&OVRS_EXCG_CD=NASD&TR_CRCY_CD=USD&CTX_AREA_FK200=&CTX_AREA_NK200="))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-value"))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header("tr_id", "VTTS3012R"))
				.andExpect(queryParam("CANO", "12345678"))
				.andExpect(queryParam("ACNT_PRDT_CD", "01"))
				.andExpect(queryParam("OVRS_EXCG_CD", "NASD"))
				.andExpect(queryParam("TR_CRCY_CD", "USD"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output1": [
						    {
						      "ovrs_pdno": "NVDA",
						      "ovrs_item_name": "NVIDIA CORP",
						      "ovrs_cblc_qty": "1.000000",
						      "ovrs_stck_evlu_amt": "180.0000"
						    }
						  ],
						  "output2": {
						    "frcr_pchs_amt1": "180.0000"
						  },
						  "ctx_area_fk200": "",
						  "ctx_area_nk200": ""
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasBalanceResponse response = client.inquireNasdaqUsdBalance(new KisAccessToken(
				"Bearer",
				"token-value",
				86400,
				"2026-08-18 22:00:00"
		));

		assertThat(response.returnCode()).isEqualTo("0");
		assertThat(response.output1()).isInstanceOf(List.class);
		List<?> holdings = (List<?>) response.output1();
		assertThat(holdings).hasSize(1);
		@SuppressWarnings("unchecked")
		Map<String, Object> holding = (Map<String, Object>) holdings.getFirst();
		assertThat(holding).containsEntry("ovrs_pdno", "NVDA");
		server.verify();
	}
}
