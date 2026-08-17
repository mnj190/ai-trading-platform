package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisOverseasOrderableAmountRequestTests {

	@Test
	void createsNasdaqOrderableAmountRequest() {
		KisOverseasOrderableAmountRequest request = KisOverseasOrderableAmountRequest.nasdaq(
				"AAPL",
				new BigDecimal("225.126")
		);

		assertThat(request.overseasExchangeCode()).isEqualTo("NASD");
		assertThat(request.overseasOrderUnitPrice()).isEqualTo("225.13");
		assertThat(request.itemCode()).isEqualTo("AAPL");
		assertThat(request.transactionContinuation()).isEmpty();
	}

	@Test
	void rejectsNonPositiveOrderUnitPrice() {
		assertThatThrownBy(() -> KisOverseasOrderableAmountRequest.nasdaq("AAPL", BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("orderUnitPrice");
	}
}
