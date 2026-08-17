package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.strategy.StrategyValuationInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KisPriceDetailValuationInputMapperTests {

	private final KisPriceDetailValuationInputMapper mapper = new KisPriceDetailValuationInputMapper();

	@Test
	void mapsKisPriceDetailOutputToStrategyValuationInput() {
		KisOverseasPriceDetailResponse response = new KisOverseasPriceDetailResponse(
				"0",
				"MCA00000",
				"정상처리 되었습니다.",
				Map.of(
						"last", "226.2107",
						"perx", "34.64",
						"epsx", "6.53"
				)
		);

		StrategyValuationInput input = mapper.toInput("NVDA", response, new BigDecimal("45.0000"));

		assertThat(input.ticker()).isEqualTo("NVDA");
		assertThat(input.closePrice()).isEqualByComparingTo("226.2107");
		assertThat(input.currentPer()).isEqualByComparingTo("34.64");
		assertThat(input.ttmEps()).isEqualByComparingTo("6.53");
		assertThat(input.fiveYearAveragePer()).isEqualByComparingTo("45.0000");
	}

	@Test
	void computesTtmEpsWhenEpsIsAbsent() {
		KisOverseasPriceDetailResponse response = new KisOverseasPriceDetailResponse(
				"0",
				"MCA00000",
				"정상처리 되었습니다.",
				Map.of(
						"last", "200.0000",
						"perx", "25.0000"
				)
		);

		StrategyValuationInput input = mapper.toInput("AAPL", response, new BigDecimal("30.0000"));

		assertThat(input.ttmEps()).isEqualByComparingTo("8.0000");
	}

	@Test
	void rejectsFailedKisResponse() {
		KisOverseasPriceDetailResponse response = new KisOverseasPriceDetailResponse(
				"1",
				"ERROR",
				"failed",
				null
		);

		assertThatThrownBy(() -> mapper.toInput("MSFT", response, BigDecimal.ONE))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("ERROR");
	}
}
