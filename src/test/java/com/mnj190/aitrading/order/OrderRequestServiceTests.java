package com.mnj190.aitrading.order;

import com.mnj190.aitrading.strategy.StrategyDecision;
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
class OrderRequestServiceTests {

	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	@Autowired
	private OrderRequestService orderRequestService;

	@Autowired
	private OrderHistoryRepository orderHistoryRepository;

	@Test
	void createsNoOrderHistoryForHoldDecision() {
		List<OrderHistory> orders = orderRequestService.createRequestedOrders(new OrderRequestCommand(
				StrategyDecision.hold(),
				new BigDecimal("1000.0000"),
				null,
				BigDecimal.ZERO,
				STRATEGY_VERSION,
				OffsetDateTime.now()
		));

		assertThat(orders).isEmpty();
		assertThat(orderHistoryRepository.findByStrategyVersionOrderByOrderedAtAsc(STRATEGY_VERSION)).isEmpty();
	}

	@Test
	void createsEntryOrderHistoryFromDecision() {
		List<OrderHistory> orders = orderRequestService.createRequestedOrders(new OrderRequestCommand(
				StrategyDecision.entry("NVDA"),
				new BigDecimal("1000.0000"),
				null,
				BigDecimal.ZERO,
				STRATEGY_VERSION,
				OffsetDateTime.now()
		));

		assertThat(orders).singleElement()
				.satisfies(order -> {
					assertThat(order.getId()).isNotNull();
					assertThat(order.getTicker()).isEqualTo("NVDA");
					assertThat(order.getSide()).isEqualTo(OrderSide.BUY);
					assertThat(order.getOrderReason()).isEqualTo(OrderReason.ENTRY);
					assertThat(order.getRequestedAmount()).isEqualByComparingTo("1000.0000");
					assertThat(order.getRequestedQuantity()).isNull();
					assertThat(order.getStatus()).isEqualTo(OrderStatus.REQUESTED);
				});
	}

	@Test
	void createsSwitchOrderHistoriesInSellThenBuyOrder() {
		List<OrderHistory> orders = orderRequestService.createRequestedOrders(new OrderRequestCommand(
				StrategyDecision.switchTo("NVDA", "AMZN"),
				new BigDecimal("100.0000"),
				new BigDecimal("2.500000"),
				new BigDecimal("900.0000"),
				STRATEGY_VERSION,
				OffsetDateTime.now()
		));

		assertThat(orders).hasSize(2);

		OrderHistory sell = orders.get(0);
		assertThat(sell.getTicker()).isEqualTo("NVDA");
		assertThat(sell.getSide()).isEqualTo(OrderSide.SELL);
		assertThat(sell.getOrderReason()).isEqualTo(OrderReason.SWITCH);
		assertThat(sell.getRequestedQuantity()).isEqualByComparingTo("2.500000");

		OrderHistory buy = orders.get(1);
		assertThat(buy.getTicker()).isEqualTo("AMZN");
		assertThat(buy.getSide()).isEqualTo(OrderSide.BUY);
		assertThat(buy.getOrderReason()).isEqualTo(OrderReason.SWITCH);
		assertThat(buy.getRequestedAmount()).isEqualByComparingTo("1000.0000");
	}
}
