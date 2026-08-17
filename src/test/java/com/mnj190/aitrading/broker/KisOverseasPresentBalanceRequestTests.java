package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisOverseasPresentBalanceRequestTests {

	@Test
	void createsAllWonRequest() {
		KisOverseasPresentBalanceRequest request = KisOverseasPresentBalanceRequest.allWon();

		assertThat(request.wonForeignCurrencyDivisionCode()).isEqualTo("01");
		assertThat(request.nationCode()).isEqualTo("000");
		assertThat(request.tradingMarketCode()).isEqualTo("00");
		assertThat(request.inquiryDivisionCode()).isEqualTo("00");
		assertThat(request.transactionContinuation()).isEmpty();
	}

	@Test
	void createsAllForeignCurrencyRequest() {
		KisOverseasPresentBalanceRequest request = KisOverseasPresentBalanceRequest.allForeignCurrency();

		assertThat(request.wonForeignCurrencyDivisionCode()).isEqualTo("02");
		assertThat(request.nationCode()).isEqualTo("000");
		assertThat(request.tradingMarketCode()).isEqualTo("00");
		assertThat(request.inquiryDivisionCode()).isEqualTo("00");
		assertThat(request.transactionContinuation()).isEmpty();
	}

	@Test
	void rejectsBlankDivisionCode() {
		assertThatThrownBy(() -> new KisOverseasPresentBalanceRequest("", "000", "00", "00", ""))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("wonForeignCurrencyDivisionCode");
	}
}
