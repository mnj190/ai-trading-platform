package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KisTokenClientTests {

	@Test
	void issuesAccessTokenWithClientCredentialsRequest() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		KisTokenClient client = new KisTokenClient(
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

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/oauth2/tokenP"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json("""
						{
						  "grant_type": "client_credentials",
						  "appkey": "test-key",
						  "appsecret": "test-secret"
						}
						"""))
				.andRespond(withSuccess("""
						{
						  "token_type": "Bearer",
						  "access_token": "access-token-value",
						  "expires_in": 86400,
						  "access_token_token_expired": "2026-08-18 22:00:00"
						}
						""", MediaType.APPLICATION_JSON));

		KisAccessToken token = client.issueAccessToken();

		assertThat(token.tokenType()).isEqualTo("Bearer");
		assertThat(token.accessToken()).isEqualTo("access-token-value");
		assertThat(token.expiresIn()).isEqualTo(86400);
		assertThat(token.accessTokenExpiredAt()).isEqualTo("2026-08-18 22:00:00");
		server.verify();
	}
}
