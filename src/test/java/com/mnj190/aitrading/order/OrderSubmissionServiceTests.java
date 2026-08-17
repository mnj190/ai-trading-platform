package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisAccessToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ActiveProfiles("local")
@SpringBootTest(properties = {
		"kis.api.app-key=test-key",
		"kis.api.app-secret=test-secret",
		"kis.api.account-number=12345678",
		"kis.api.account-product-code=01",
		"kis.api.paper-trading=true"
})
@Transactional
class OrderSubmissionServiceTests {

	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	@Autowired
	private OrderHistoryRepository orderHistoryRepository;

	@Test
	void submitsRequestedOrderAndStoresBrokerOrderId() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		OrderSubmissionService orderSubmissionService = orderSubmissionService(restClientBuilder);
		OrderHistory order = orderHistoryRepository.saveAndFlush(requestedBuyOrder());
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
				.andExpect(content().json(expectedBody))
				.andRespond(withSuccess("""
						{
						  "HASH": "hash-key-value"
						}
						""", MediaType.APPLICATION_JSON));

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/order"))
				.andExpect(header("tr_id", "VTTT1002U"))
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

		OrderHistory submitted = orderSubmissionService.submit(new OrderSubmissionCommand(
				order.getId(),
				token(),
				BigDecimal.ONE,
				new BigDecimal("180.12")
		));

		assertThat(submitted.getStatus()).isEqualTo(OrderStatus.SUBMITTED);
		assertThat(submitted.getBrokerOrderId()).isEqualTo("0000000001");
		server.verify();
	}

	@Test
	void marksOrderRejectedWhenKisRejectsOrder() {
		RestClient.Builder restClientBuilder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
		OrderSubmissionService orderSubmissionService = orderSubmissionService(restClientBuilder);
		OrderHistory order = orderHistoryRepository.saveAndFlush(requestedBuyOrder());

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/hashkey"))
				.andRespond(withSuccess("""
						{
						  "HASH": "hash-key-value"
						}
						""", MediaType.APPLICATION_JSON));

		server.expect(once(), requestTo("https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/order"))
				.andRespond(withSuccess("""
						{
						  "rt_cd": "1",
						  "msg_cd": "APBK9999",
						  "msg1": "주문 거부",
						  "output": {}
						}
						""", MediaType.APPLICATION_JSON));

		OrderHistory rejected = orderSubmissionService.submit(new OrderSubmissionCommand(
				order.getId(),
				token(),
				BigDecimal.ONE,
				new BigDecimal("180.12")
		));

		assertThat(rejected.getStatus()).isEqualTo(OrderStatus.REJECTED);
		assertThat(rejected.getBrokerOrderId()).isNull();
		server.verify();
	}

	private OrderHistory requestedBuyOrder() {
		return new OrderHistory(
				"NVDA",
				OrderSide.BUY,
				OrderReason.ENTRY,
				new BigDecimal("1000.0000"),
				null,
				OrderType.MARKET,
				OrderStatus.REQUESTED,
				STRATEGY_VERSION,
				OffsetDateTime.now()
		);
	}

	private OrderSubmissionService orderSubmissionService(RestClient.Builder restClientBuilder) {
		var properties = new com.mnj190.aitrading.broker.KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"test-key",
				"test-secret",
				"12345678",
				"01",
				true
		);
		var hashKeyClient = new com.mnj190.aitrading.broker.KisHashKeyClient(properties, restClientBuilder);
		var orderClient = new com.mnj190.aitrading.broker.KisOverseasOrderClient(
				properties,
				hashKeyClient,
				restClientBuilder
		);
		return new OrderSubmissionService(orderHistoryRepository, orderClient);
	}

	private KisAccessToken token() {
		return new KisAccessToken(
				"Bearer",
				"token-value",
				86400,
				"2026-08-18 22:00:00"
		);
	}
}
