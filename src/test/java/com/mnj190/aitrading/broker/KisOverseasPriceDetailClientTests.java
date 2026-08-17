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

class KisOverseasPriceDetailClientTests {

	@Test
	void inquiresOverseasPriceDetailWithRequiredHeadersAndParams() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisOverseasPriceDetailClient client = new KisOverseasPriceDetailClient(properties(), builder);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-price/v1/quotations/price-detail?AUTH=&EXCD=NAS&SYMB=AAPL"))
				.andExpect(method(HttpMethod.GET))
				.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-value"))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header("tr_id", "HHDFS76200200"))
				.andExpect(header("tr_cont", ""))
				.andExpect(queryParam("AUTH", ""))
				.andExpect(queryParam("EXCD", "NAS"))
				.andExpect(queryParam("SYMB", "AAPL"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "0",
						  "msg_cd": "MCA00000",
						  "msg1": "정상처리 되었습니다.",
						  "output": {
						    "last": "303.6900"
						  }
						}
						""", MediaType.APPLICATION_JSON));

		KisOverseasPriceDetailResponse response = client.inquireNasdaqPriceDetail(token(), "AAPL");

		assertThat(response.returnCode()).isEqualTo("0");
		assertThat(response.message()).isEqualTo("정상처리 되었습니다.");
		assertThat(response.output()).isInstanceOf(Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> output = (Map<String, Object>) response.output();
		assertThat(output).containsEntry("last", "303.6900");
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
