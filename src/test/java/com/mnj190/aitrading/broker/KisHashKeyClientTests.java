package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisHashKeyClientTests {

	@Test
	void issuesHashKeyForPostBodyWithExplicitContentLength() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		ObjectMapper objectMapper = new ObjectMapper();
		KisHashKeyClient client = new KisHashKeyClient(properties(), builder, objectMapper);
		Map<String, String> requestBody = Map.of("PDNO", "NVDA", "ORD_QTY", "1");
		String expectedContentLength = String.valueOf(objectMapper.writeValueAsBytes(requestBody).length);

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/hashkey"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("appkey", "test-key"))
				.andExpect(header("appsecret", "test-secret"))
				.andExpect(header(HttpHeaders.CONTENT_LENGTH, expectedContentLength))
				.andExpect(content().json("""
						{
						  "PDNO": "NVDA",
						  "ORD_QTY": "1"
						}
						"""))
				.andRespond(withSuccess("""
						{
						  "HASH": "hash-key-value"
						}
						""", MediaType.APPLICATION_JSON));

		String hashKey = client.issueHashKey(requestBody);

		assertThat(hashKey).isEqualTo("hash-key-value");
		server.verify();
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
