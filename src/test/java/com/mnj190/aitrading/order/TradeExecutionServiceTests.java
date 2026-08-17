package com.mnj190.aitrading.order;

import com.mnj190.aitrading.portfolio.PositionState;
import com.mnj190.aitrading.portfolio.PositionStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class TradeExecutionServiceTests {

	@Autowired
	private TradeExecutionService tradeExecutionService;

	@Autowired
	private OrderHistoryRepository orderHistoryRepository;

	@Autowired
	private TradeHistoryRepository tradeHistoryRepository;

	@Autowired
	private PositionStateRepository positionStateRepository;

	@Test
	void recordsBuyFillAndCreatesPosition() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_FILL_BUY_TEST";
		OrderHistory order = orderHistoryRepository.saveAndFlush(submittedOrder(
				"NVDA",
				OrderSide.BUY,
				OrderReason.ENTRY,
				strategyVersion,
				new BigDecimal("2.000000")
		));

		TradeHistory trade = tradeExecutionService.recordFill(new TradeExecutionCommand(
				order.getId(),
				new BigDecimal("2.000000"),
				new BigDecimal("180.1250"),
				OffsetDateTime.parse("2026-08-18T22:10:00+09:00")
		)).orElseThrow();

		assertThat(trade.getExecutedAmount()).isEqualByComparingTo("360.2500");
		assertThat(orderHistoryRepository.findById(order.getId()).orElseThrow().getStatus())
				.isEqualTo(OrderStatus.FILLED);
		PositionState position = positionStateRepository.findByStrategyVersion(strategyVersion).orElseThrow();
		assertThat(position.getTicker()).isEqualTo("NVDA");
		assertThat(position.getQuantity()).isEqualByComparingTo("2.000000");
		assertThat(position.getAveragePrice()).isEqualByComparingTo("180.1250");
		assertThat(position.getInvestedAmount()).isEqualByComparingTo("360.2500");
	}

	@Test
	void recordsFullSellFillAndClosesPosition() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_FILL_SELL_TEST";
		positionStateRepository.saveAndFlush(new PositionState(
				"NVDA",
				new BigDecimal("2.000000"),
				new BigDecimal("180.0000"),
				new BigDecimal("360.0000"),
				strategyVersion
		));
		OrderHistory order = orderHistoryRepository.saveAndFlush(submittedOrder(
				"NVDA",
				OrderSide.SELL,
				OrderReason.EXIT,
				strategyVersion,
				new BigDecimal("2.000000")
		));

		TradeHistory trade = tradeExecutionService.recordFill(new TradeExecutionCommand(
				order.getId(),
				new BigDecimal("2.000000"),
				new BigDecimal("190.0000"),
				OffsetDateTime.parse("2026-08-18T22:10:00+09:00")
		)).orElseThrow();

		assertThat(tradeHistoryRepository.findByOrder_IdOrderByExecutedAtAsc(order.getId()))
				.containsExactly(trade);
		assertThat(orderHistoryRepository.findById(order.getId()).orElseThrow().getStatus())
				.isEqualTo(OrderStatus.FILLED);
		assertThat(positionStateRepository.findByStrategyVersion(strategyVersion)).isEmpty();
	}

	@Test
	void recordsPartialFillThenCompletesOnSecondCumulativeReport() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_PARTIAL_FILL_TEST";
		OrderHistory order = orderHistoryRepository.saveAndFlush(submittedOrder(
				"NVDA",
				OrderSide.BUY,
				OrderReason.ENTRY,
				strategyVersion,
				new BigDecimal("5.000000")
		));

		TradeHistory firstTrade = tradeExecutionService.recordFill(new TradeExecutionCommand(
				order.getId(),
				new BigDecimal("2.000000"),
				new BigDecimal("180.0000"),
				OffsetDateTime.parse("2026-08-18T22:10:00+09:00")
		)).orElseThrow();

		assertThat(firstTrade.getExecutedQuantity()).isEqualByComparingTo("2.000000");
		assertThat(orderHistoryRepository.findById(order.getId()).orElseThrow().getStatus())
				.isEqualTo(OrderStatus.PARTIALLY_FILLED);
		assertThat(positionStateRepository.findByStrategyVersion(strategyVersion).orElseThrow().getQuantity())
				.isEqualByComparingTo("2.000000");

		TradeHistory secondTrade = tradeExecutionService.recordFill(new TradeExecutionCommand(
				order.getId(),
				new BigDecimal("5.000000"),
				new BigDecimal("181.0000"),
				OffsetDateTime.parse("2026-08-18T22:20:00+09:00")
		)).orElseThrow();

		assertThat(secondTrade.getExecutedQuantity()).isEqualByComparingTo("3.000000");
		assertThat(orderHistoryRepository.findById(order.getId()).orElseThrow().getStatus())
				.isEqualTo(OrderStatus.FILLED);
		assertThat(tradeHistoryRepository.findByOrder_IdOrderByExecutedAtAsc(order.getId())).hasSize(2);
		assertThat(positionStateRepository.findByStrategyVersion(strategyVersion).orElseThrow().getQuantity())
				.isEqualByComparingTo("5.000000");
	}

	@Test
	void ignoresDuplicateCumulativeReportForSameOrder() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_DUPLICATE_FILL_TEST";
		OrderHistory order = orderHistoryRepository.saveAndFlush(submittedOrder(
				"NVDA",
				OrderSide.BUY,
				OrderReason.ENTRY,
				strategyVersion,
				new BigDecimal("2.000000")
		));

		tradeExecutionService.recordFill(new TradeExecutionCommand(
				order.getId(),
				new BigDecimal("2.000000"),
				new BigDecimal("180.0000"),
				OffsetDateTime.parse("2026-08-18T22:10:00+09:00")
		)).orElseThrow();

		Optional<TradeHistory> secondSync = tradeExecutionService.recordFill(new TradeExecutionCommand(
				order.getId(),
				new BigDecimal("2.000000"),
				new BigDecimal("180.0000"),
				OffsetDateTime.parse("2026-08-18T22:30:00+09:00")
		));

		assertThat(secondSync).isEmpty();
		assertThat(tradeHistoryRepository.findByOrder_IdOrderByExecutedAtAsc(order.getId())).hasSize(1);
		assertThat(positionStateRepository.findByStrategyVersion(strategyVersion).orElseThrow().getQuantity())
				.isEqualByComparingTo("2.000000");
	}

	private OrderHistory submittedOrder(
			String ticker,
			OrderSide side,
			OrderReason reason,
			String strategyVersion,
			BigDecimal submittedQuantity
	) {
		OrderHistory order = new OrderHistory(
				ticker,
				side,
				reason,
				side == OrderSide.BUY ? new BigDecimal("1000.0000") : BigDecimal.ZERO,
				side == OrderSide.SELL ? new BigDecimal("2.000000") : null,
				OrderType.MARKET,
				OrderStatus.REQUESTED,
				strategyVersion,
				OffsetDateTime.parse("2026-08-18T22:00:00+09:00")
		);
		order.markSubmitted("broker-" + strategyVersion, submittedQuantity);
		return order;
	}
}
