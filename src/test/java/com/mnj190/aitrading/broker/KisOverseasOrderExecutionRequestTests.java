package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisOverseasOrderExecutionRequestTests {

	@Test
	void createsAllExecutionsInquiryForDate() {
		KisOverseasOrderExecutionRequest request = KisOverseasOrderExecutionRequest.allForDate(
				LocalDate.of(2026, 8, 17)
		);

		assertThat(request.productNumber()).isEmpty();
		assertThat(request.formattedOrderStartDate()).isEqualTo("20260817");
		assertThat(request.formattedOrderEndDate()).isEqualTo("20260817");
		assertThat(request.sellBuyDivision()).isEqualTo("00");
		assertThat(request.executionDivision()).isEqualTo("00");
		assertThat(request.overseasExchangeCode()).isEmpty();
		assertThat(request.sortSequence()).isEqualTo("DS");
		assertThat(request.orderDate()).isEmpty();
		assertThat(request.orderBranchNumber()).isEmpty();
		assertThat(request.orderNumber()).isEmpty();
		assertThat(request.contextAreaNk200()).isEmpty();
		assertThat(request.contextAreaFk200()).isEmpty();
		assertThat(request.transactionContinuation()).isEmpty();
	}

	@Test
	void rejectsBlankSortSequence() {
		assertThatThrownBy(() -> new KisOverseasOrderExecutionRequest(
				"",
				LocalDate.of(2026, 8, 17),
				LocalDate.of(2026, 8, 17),
				"00",
				"00",
				"",
				" ",
				"",
				"",
				"",
				"",
				"",
				""
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("sortSequence");
	}
}
