package com.mnj190.aitrading.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class OrderHistoryRepositoryTests {

	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	@Autowired
	private OrderHistoryRepository repository;

	@Test
	void savesFindsUpdatesAndDeletesOrderHistory() {
		OrderHistory order = new OrderHistory(
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

		OrderHistory saved = repository.saveAndFlush(order);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(saved.getUpdatedAt()).isNotNull();

		saved.markSubmitted("KIS-ORDER-1", new BigDecimal("5.000000"));
		repository.saveAndFlush(saved);

		OrderHistory reloaded = repository.findByBrokerOrderId("KIS-ORDER-1").orElseThrow();
		assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.SUBMITTED);
		assertThat(reloaded.getSubmittedQuantity()).isEqualByComparingTo("5.000000");
		assertThat(reloaded.getFilledQuantity()).isEqualByComparingTo("0");

		List<OrderHistory> orders = repository.findByStrategyVersionOrderByOrderedAtAsc(STRATEGY_VERSION);
		assertThat(orders).extracting(OrderHistory::getTicker).contains("NVDA");

		repository.delete(saved);
		repository.flush();

		assertThat(repository.existsById(saved.getId())).isFalse();
	}
}
