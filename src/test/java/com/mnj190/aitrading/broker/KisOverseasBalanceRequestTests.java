package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisOverseasBalanceRequestTests {

	@Test
	void createsNasdaqUsdFirstPageRequest() {
		KisOverseasBalanceRequest request = KisOverseasBalanceRequest.nasdaqUsdFirstPage();

		assertThat(request.overseasExchangeCode()).isEqualTo("NASD");
		assertThat(request.transactionCurrencyCode()).isEqualTo("USD");
		assertThat(request.contextAreaFk200()).isEmpty();
		assertThat(request.contextAreaNk200()).isEmpty();
		assertThat(request.transactionContinuation()).isEmpty();
	}

	@Test
	void rejectsBlankExchangeCode() {
		assertThatThrownBy(() -> new KisOverseasBalanceRequest("", "USD", "", "", ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("overseasExchangeCode");
	}
}
