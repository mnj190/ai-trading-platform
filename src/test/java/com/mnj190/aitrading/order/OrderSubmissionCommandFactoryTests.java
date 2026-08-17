package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisAccessToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class OrderSubmissionCommandFactoryTests {

	@Autowired
	private OrderSubmissionCommandFactory factory;

	@Autowired
	private OrderHistoryRepository orderHistoryRepository;

	@Test
	void createsBuySubmissionCommandUsingWholeShareQuantity() {
		OrderHistory order = orderHistoryRepository.saveAndFlush(requestedOrder(
				OrderSide.BUY,
				new BigDecimal("1000.0000"),
				null
		));

		OrderSubmissionCommand command = factory.create(order, token(), new BigDecimal("180.1200"));

		assertThat(command.orderId()).isEqualTo(order.getId());
		assertThat(command.orderQuantity()).isEqualByComparingTo("5");
		assertThat(command.limitPrice()).isEqualByComparingTo("180.1200");
	}

	@Test
	void createsSellSubmissionCommandUsingRequestedQuantity() {
		OrderHistory order = orderHistoryRepository.saveAndFlush(requestedOrder(
				OrderSide.SELL,
				BigDecimal.ZERO,
				new BigDecimal("2.500000")
		));

		OrderSubmissionCommand command = factory.create(order, token(), new BigDecimal("180.1200"));

		assertThat(command.orderQuantity()).isEqualByComparingTo("2.500000");
	}

	@Test
	void rejectsBuyWhenAmountIsLessThanOneShare() {
		OrderHistory order = orderHistoryRepository.saveAndFlush(requestedOrder(
				OrderSide.BUY,
				new BigDecimal("100.0000"),
				null
		));

		assertThatThrownBy(() -> factory.create(order, token(), new BigDecimal("180.1200")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("quantity");
	}

	private OrderHistory requestedOrder(
			OrderSide side,
			BigDecimal requestedAmount,
			BigDecimal requestedQuantity
	) {
		return new OrderHistory(
				"NVDA",
				side,
				side == OrderSide.BUY ? OrderReason.ENTRY : OrderReason.EXIT,
				requestedAmount,
				requestedQuantity,
				OrderType.MARKET,
				OrderStatus.REQUESTED,
				"PE_MEAN_REVERSION_V1",
				OffsetDateTime.parse("2026-08-18T22:00:00+09:00")
		);
	}

	private KisAccessToken token() {
		return new KisAccessToken(
				"Bearer",
				"token-value",
				86400,
				"2026-08-18 22:00:00"
		);
	}
}
