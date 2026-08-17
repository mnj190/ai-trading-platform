package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisOverseasPriceRequestTests {

	@Test
	void createsNasdaqPriceRequest() {
		KisOverseasPriceRequest request = KisOverseasPriceRequest.nasdaq("AAPL");

		assertThat(request.auth()).isEmpty();
		assertThat(request.exchangeCode()).isEqualTo("NAS");
		assertThat(request.symbol()).isEqualTo("AAPL");
		assertThat(request.transactionContinuation()).isEmpty();
	}

	@Test
	void rejectsBlankSymbol() {
		assertThatThrownBy(() -> KisOverseasPriceRequest.nasdaq(" "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("symbol");
	}
}
