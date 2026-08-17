package com.mnj190.aitrading.strategy;

import com.mnj190.aitrading.market.ValuationSnapshotRepository;
import com.mnj190.aitrading.order.OrderHistory;
import com.mnj190.aitrading.order.OrderReason;
import com.mnj190.aitrading.order.OrderSide;
import com.mnj190.aitrading.order.OrderStatus;
import com.mnj190.aitrading.portfolio.PositionState;
import com.mnj190.aitrading.portfolio.PositionStateRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class DailyEvaluationServiceTests {

	private static final LocalDate TRADING_DATE = LocalDate.of(2026, 8, 18);

	@Autowired
	private DailyEvaluationService dailyEvaluationService;

	@Autowired
	private StrategyConfigRepository strategyConfigRepository;

	@Autowired
	private PositionStateRepository positionStateRepository;

	@Autowired
	private ValuationSnapshotRepository valuationSnapshotRepository;

	@Test
	void recordsValuationSnapshotsAndCreatesEntryOrderWhenCash() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_DAILY_ENTRY_TEST";
		saveEnabledConfig(strategyVersion);

		DailyEvaluationReport report = dailyEvaluationService.evaluateAndCreateOrderRequests(new DailyEvaluationCommand(
				TRADING_DATE,
				List.of(
						input("NVDA", "80.0000"),
						input("GOOGL", "100.0000"),
						input("AAPL", "100.0000"),
						input("AMZN", "100.0000"),
						input("MSFT", "100.0000")
				),
				new BigDecimal("1000.0000"),
				strategyVersion,
				OffsetDateTime.parse("2026-08-18T22:00:00+09:00")
		));

		assertThat(report.currentHoldingTicker()).isEmpty();
		assertThat(report.evaluation().decision().signal()).isEqualTo(StrategySignal.ENTRY);
		assertThat(report.evaluation().decision().buyTicker()).isEqualTo("NVDA");
		assertThat(report.requestedOrders()).singleElement()
				.satisfies(order -> {
					assertThat(order.getTicker()).isEqualTo("NVDA");
					assertThat(order.getSide()).isEqualTo(OrderSide.BUY);
					assertThat(order.getOrderReason()).isEqualTo(OrderReason.ENTRY);
					assertThat(order.getRequestedAmount()).isEqualByComparingTo("1000.0000");
					assertThat(order.getStatus()).isEqualTo(OrderStatus.REQUESTED);
				});
		assertThat(valuationSnapshotRepository
				.findByTradingDateAndStrategyVersionOrderByTicker(TRADING_DATE, strategyVersion)).hasSize(5);
	}

	@Test
	void createsSwitchOrdersUsingCurrentHoldingMarketValue() {
		String strategyVersion = "PE_MEAN_REVERSION_V1_DAILY_SWITCH_TEST";
		saveEnabledConfig(strategyVersion);
		positionStateRepository.saveAndFlush(new PositionState(
				"NVDA",
				new BigDecimal("2.000000"),
				new BigDecimal("85.0000"),
				new BigDecimal("170.0000"),
				strategyVersion
		));

		DailyEvaluationReport report = dailyEvaluationService.evaluateAndCreateOrderRequests(new DailyEvaluationCommand(
				TRADING_DATE,
				List.of(
						input("NVDA", "90.0000"),
						input("AMZN", "70.0000"),
						input("GOOGL", "100.0000"),
						input("AAPL", "100.0000"),
						input("MSFT", "100.0000")
				),
				new BigDecimal("100.0000"),
				strategyVersion,
				OffsetDateTime.parse("2026-08-18T22:00:00+09:00")
		));

		assertThat(report.currentHoldingTicker()).contains("NVDA");
		assertThat(report.evaluation().decision().signal()).isEqualTo(StrategySignal.SWITCH);
		assertThat(report.requestedOrders()).hasSize(2);

		OrderHistory sell = report.requestedOrders().get(0);
		assertThat(sell.getTicker()).isEqualTo("NVDA");
		assertThat(sell.getSide()).isEqualTo(OrderSide.SELL);
		assertThat(sell.getOrderReason()).isEqualTo(OrderReason.SWITCH);
		assertThat(sell.getRequestedQuantity()).isEqualByComparingTo("2.000000");

		OrderHistory buy = report.requestedOrders().get(1);
		assertThat(buy.getTicker()).isEqualTo("AMZN");
		assertThat(buy.getSide()).isEqualTo(OrderSide.BUY);
		assertThat(buy.getOrderReason()).isEqualTo(OrderReason.SWITCH);
		assertThat(buy.getRequestedAmount()).isEqualByComparingTo("280.0000");
	}

	private void saveEnabledConfig(String strategyVersion) {
		strategyConfigRepository.saveAndFlush(new StrategyConfig(
				strategyVersion,
				new BigDecimal("-0.1500"),
				new BigDecimal("0.0500"),
				new BigDecimal("0.0000"),
				1,
				true
		));
	}

	private StrategyValuationInput input(String ticker, String currentPer) {
		return new StrategyValuationInput(
				ticker,
				new BigDecimal(currentPer),
				BigDecimal.ONE,
				new BigDecimal(currentPer),
				new BigDecimal("100.0000")
		);
	}
}
