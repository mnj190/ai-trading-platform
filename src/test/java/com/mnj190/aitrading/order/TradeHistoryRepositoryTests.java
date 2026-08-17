package com.mnj190.aitrading.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class TradeHistoryRepositoryTests {

	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	@Autowired
	private OrderHistoryRepository orderHistoryRepository;

	@Autowired
	private TradeHistoryRepository tradeHistoryRepository;

	@Test
	void savesFindsAndDeletesTradeHistory() {
		OrderHistory order = orderHistoryRepository.saveAndFlush(new OrderHistory(
				"NVDA",
				OrderSide.BUY,
				OrderReason.ENTRY,
				new BigDecimal("1000.0000"),
				null,
				OrderType.MARKET,
				OrderStatus.SUBMITTED,
				STRATEGY_VERSION,
				OffsetDateTime.now()
		));

		TradeHistory trade = new TradeHistory(
				order,
				"NVDA",
				OrderSide.BUY,
				OrderReason.ENTRY,
				new BigDecimal("5.000000"),
				new BigDecimal("200.0000"),
				new BigDecimal("1000.0000"),
				OffsetDateTime.now()
		);

		TradeHistory saved = tradeHistoryRepository.saveAndFlush(trade);

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getCreatedAt()).isNotNull();
		assertThat(tradeHistoryRepository.findByOrder_IdOrderByExecutedAtAsc(order.getId()))
				.extracting(TradeHistory::getTicker)
				.containsExactly("NVDA");

		tradeHistoryRepository.delete(saved);
		tradeHistoryRepository.flush();

		assertThat(tradeHistoryRepository.existsById(saved.getId())).isFalse();
	}
}
