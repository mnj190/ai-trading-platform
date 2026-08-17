package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisOverseasOrderRequestTests {

	@Test
	void createsUsLimitOrderBody() {
		KisOverseasOrderRequest request = KisOverseasOrderRequest.usLimitOrder(
				"NVDA",
				new BigDecimal("1"),
				new BigDecimal("180.123")
		);

		Map<String, String> body = request.toBody(new KisApiProperties(
				"https://openapivts.koreainvestment.com:29443",
				"test-key",
				"test-secret",
				"12345678",
				"01",
				true
		));

		assertThat(body)
				.containsEntry("CANO", "12345678")
				.containsEntry("ACNT_PRDT_CD", "01")
				.containsEntry("OVRS_EXCG_CD", "NASD")
				.containsEntry("PDNO", "NVDA")
				.containsEntry("ORD_DVSN", "00")
				.containsEntry("ORD_QTY", "1")
				.containsEntry("OVRS_ORD_UNPR", "180.12")
				.containsEntry("ORD_SVR_DVSN_CD", "0");
	}

	@Test
	void rejectsNonPositiveLimitPrice() {
		assertThatThrownBy(() -> KisOverseasOrderRequest.usLimitOrder(
				"NVDA",
				new BigDecimal("1"),
				BigDecimal.ZERO
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("price");
	}
}
