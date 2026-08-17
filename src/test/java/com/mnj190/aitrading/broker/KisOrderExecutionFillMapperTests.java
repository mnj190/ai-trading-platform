package com.mnj190.aitrading.broker;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisOrderExecutionFillMapperTests {

	private final KisOrderExecutionFillMapper mapper = new KisOrderExecutionFillMapper();

	@Test
	void mapsSuccessfulExecutionRowsToFills() {
		OffsetDateTime fallbackExecutedAt = OffsetDateTime.parse("2026-08-18T22:10:00+09:00");
		KisOverseasOrderExecutionResponse response = new KisOverseasOrderExecutionResponse(
				"0",
				"APBK0013",
				"정상처리 되었습니다.",
				List.of(Map.of(
						"odno", "0000000001",
						"pdno", "NVDA",
						"sll_buy_dvsn_cd", "02",
						"ft_ccld_qty", "2",
						"ft_ccld_unpr3", "180.1200"
				)),
				"",
				""
		);

		List<KisOrderExecutionFill> fills = mapper.toFills(response, fallbackExecutedAt);

		assertThat(fills).singleElement()
				.satisfies(fill -> {
					assertThat(fill.brokerOrderId()).isEqualTo("0000000001");
					assertThat(fill.ticker()).isEqualTo("NVDA");
					assertThat(fill.executedQuantity()).isEqualByComparingTo(new BigDecimal("2"));
					assertThat(fill.executedPrice()).isEqualByComparingTo("180.1200");
					assertThat(fill.executedAt()).isEqualTo(fallbackExecutedAt);
				});
	}

	@Test
	void skipsRowsWithoutPositiveFillQuantity() {
		KisOverseasOrderExecutionResponse response = new KisOverseasOrderExecutionResponse(
				"0",
				"APBK0013",
				"정상처리 되었습니다.",
				List.of(Map.of(
						"odno", "0000000001",
						"pdno", "NVDA",
						"ft_ccld_qty", "0",
						"ft_ccld_unpr3", "180.1200"
				)),
				"",
				""
		);

		assertThat(mapper.toFills(response, OffsetDateTime.now())).isEmpty();
	}

	@Test
	void rejectsFailedKisResponse() {
		KisOverseasOrderExecutionResponse response = new KisOverseasOrderExecutionResponse(
				"1",
				"ERROR",
				"failed",
				List.of(),
				"",
				""
		);

		assertThatThrownBy(() -> mapper.toFills(response, OffsetDateTime.now()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ERROR");
	}
}
