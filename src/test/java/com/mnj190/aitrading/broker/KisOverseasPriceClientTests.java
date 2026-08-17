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

class KisOverseasPriceClientTests {

	@Test
	void inquiresOverseasPriceWithRequiredHeadersAndParams() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasPriceClient client = new KisOverseasPriceClient(properties(), builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-price/v1/quotations/price?AUTH=&EXCD=NAS&SYMB=AAPL"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-value"))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header("tr_id", "HHDFS00000300"))
				.andExpect(header("tr_cont", ""))
				.andExpect(queryParam("AUTH", ""))
				.andExpect(queryParam("EXCD", "NAS"))
				.andExpect(queryParam("SYMB", "AAPL"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "APBK0013",
						  "msg1": "정상처리 되었습니다.",
						  "output": {
						    "last": "225.1200",
						    "base": "AAPL"
						  }
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasPriceResponse response = client.inquireNasdaqPrice(token(), "AAPL");

		assertThat(response.returnCode()).isEqualTo("0");
		assertThat(response.output()).isInstanceOf(Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> output = (Map<String, Object>) response.output();
		assertThat(output).containsEntry("base", "AAPL");
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

	private KisApiProperties properties() {
		return new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"test-key",
				"test-secret",
				"12345678",
				"01",
				true
		);
	}
}
