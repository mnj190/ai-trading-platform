package com.mnj190.aitrading.order;

import com.mnj190.aitrading.strategy.StrategyDecision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderCandidateFactoryTests {

	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	private final OrderCandidateFactory factory = new OrderCandidateFactory();

	@Test
	void createsNoOrderCandidatesForHold() {
		List<OrderCandidate> candidates = factory.create(
				StrategyDecision.hold(),
				new BigDecimal("1000.0000"),
				null,
				BigDecimal.ZERO,
				STRATEGY_VERSION
		);

		assertThat(candidates).isEmpty();
	}

	@Test
	void createsBuyCandidateForEntryUsingAvailableCash() {
		List<OrderCandidate> candidates = factory.create(
				StrategyDecision.entry("NVDA"),
				new BigDecimal("1000.0000"),
				null,
				BigDecimal.ZERO,
				STRATEGY_VERSION
		);

		assertThat(candidates).singleElement()
				.satisfies(candidate -> {
					assertThat(candidate.ticker()).isEqualTo("NVDA");
					assertThat(candidate.side()).isEqualTo(OrderSide.BUY);
					assertThat(candidate.reason()).isEqualTo(OrderReason.ENTRY);
					assertThat(candidate.requestedAmount()).isEqualByComparingTo("1000.0000");
					assertThat(candidate.requestedQuantity()).isNull();
					assertThat(candidate.orderType()).isEqualTo(OrderType.MARKET);
				});
	}

	@Test
	void createsSellThenBuyCandidatesForSwitch() {
		List<OrderCandidate> candidates = factory.create(
				StrategyDecision.switchTo("NVDA", "AMZN"),
				new BigDecimal("100.0000"),
				new BigDecimal("2.500000"),
				new BigDecimal("900.0000"),
				STRATEGY_VERSION
		);

		assertThat(candidates).hasSize(2);

		OrderCandidate sell = candidates.get(0);
		assertThat(sell.ticker()).isEqualTo("NVDA");
		assertThat(sell.side()).isEqualTo(OrderSide.SELL);
		assertThat(sell.reason()).isEqualTo(OrderReason.SWITCH);
		assertThat(sell.requestedQuantity()).isEqualByComparingTo("2.500000");

		OrderCandidate buy = candidates.get(1);
		assertThat(buy.ticker()).isEqualTo("AMZN");
		assertThat(buy.side()).isEqualTo(OrderSide.BUY);
		assertThat(buy.reason()).isEqualTo(OrderReason.SWITCH);
		assertThat(buy.requestedAmount()).isEqualByComparingTo("1000.0000");
	}

	@Test
	void createsSellCandidateForExit() {
		List<OrderCandidate> candidates = factory.create(
				StrategyDecision.exit("MSFT"),
				BigDecimal.ZERO,
				new BigDecimal("1.200000"),
				new BigDecimal("500.0000"),
				STRATEGY_VERSION
		);

		assertThat(candidates).singleElement()
				.satisfies(candidate -> {
					assertThat(candidate.ticker()).isEqualTo("MSFT");
					assertThat(candidate.side()).isEqualTo(OrderSide.SELL);
					assertThat(candidate.reason()).isEqualTo(OrderReason.EXIT);
					assertThat(candidate.requestedAmount()).isEqualByComparingTo("0.0000");
					assertThat(candidate.requestedQuantity()).isEqualByComparingTo("1.200000");
				});
	}

	@Test
	void rejectsSellCandidateWithoutCurrentHoldingQuantity() {
		assertThatThrownBy(() -> factory.create(
				StrategyDecision.exit("MSFT"),
				BigDecimal.ZERO,
				null,
				new BigDecimal("500.0000"),
				STRATEGY_VERSION
		)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("currentHoldingQuantity");
	}
}
