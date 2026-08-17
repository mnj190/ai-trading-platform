package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisOverseasOrderExecutionResponse;
import com.mnj190.aitrading.portfolio.PositionState;
import com.mnj190.aitrading.portfolio.PositionStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class KisOrderExecutionSyncServiceTests {

	@Autowired
	private KisOrderExecutionSyncService syncService;

	@Autowired
	private OrderHistoryRepository orderHistoryRepository;

	@Autowired
	private TradeHistoryRepository tradeHistoryRepository;

	@Autowired
	private PositionStateRepository positionStateRepository;

	@Test
	void syncsKisExecutionFillIntoTradeAndPosition() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_KIS_SYNC_TEST";
		OrderHistory order = orderHistoryRepository.saveAndFlush(submittedBuyOrder(strategyVersion));

		ExecutionSyncReport report = syncService.sync(new KisOverseasOrderExecutionResponse(
						"0",
						"APBK0013",
						"정상처리 되었습니다.",
						List.of(Map.of(
								"odno", "0000000001",
								"pdno", "NVDA",
								"ft_ccld_qty", "2",
								"ft_ccld_unpr3", "180.1200"
						)),
						"",
						""
				),
				OffsetDateTime.parse("2026-08-18T22:10:00+09:00")
		);

		assertThat(report).isEqualTo(new ExecutionSyncReport(1, 1, 0, 0));
		assertThat(tradeHistoryRepository.findByOrder_IdOrderByExecutedAtAsc(order.getId())).hasSize(1);
		assertThat(orderHistoryRepository.findById(order.getId()).orElseThrow().getStatus())
				.isEqualTo(OrderStatus.FILLED);
		PositionState position = positionStateRepository.findByStrategyVersion(strategyVersion).orElseThrow();
		assertThat(position.getTicker()).isEqualTo("NVDA");
		assertThat(position.getQuantity()).isEqualByComparingTo("2.000000");
		assertThat(position.getAveragePrice()).isEqualByComparingTo("180.1200");
	}

	@Test
	void skipsUnknownAndAlreadyProcessedOrders() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_KIS_SKIP_TEST";
		OrderHistory filledOrder = orderHistoryRepository.saveAndFlush(submittedBuyOrder(strategyVersion));
		filledOrder.markFilled();
		orderHistoryRepository.saveAndFlush(filledOrder);

		ExecutionSyncReport report = syncService.sync(new KisOverseasOrderExecutionResponse(
						"0",
						"APBK0013",
						"정상처리 되었습니다.",
						List.of(
								Map.of(
										"odno", "0000000001",
										"pdno", "NVDA",
										"ft_ccld_qty", "2",
										"ft_ccld_unpr3", "180.1200"
								),
								Map.of(
										"odno", "unknown-order",
										"pdno", "AAPL",
										"ft_ccld_qty", "1",
										"ft_ccld_unpr3", "200.0000"
								)
						),
						"",
						""
				),
				OffsetDateTime.parse("2026-08-18T22:10:00+09:00")
		);

		assertThat(report).isEqualTo(new ExecutionSyncReport(2, 0, 1, 1));
	}

	private OrderHistory submittedBuyOrder(String strategyVersion) {
		OrderHistory order = new OrderHistory(
				"NVDA",
				OrderSide.BUY,
				OrderReason.ENTRY,
				new BigDecimal("1000.0000"),
				null,
				OrderType.MARKET,
				OrderStatus.REQUESTED,
				strategyVersion,
				OffsetDateTime.parse("2026-08-18T22:00:00+09:00")
		);
		order.markSubmitted("0000000001");
		return order;
	}
}
