package com.mnj190.aitrading.strategy;

import com.mnj190.aitrading.market.ValuationSnapshot;
import com.mnj190.aitrading.market.ValuationSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class StrategyEvaluationRecorderTests {

	private static final LocalDate TRADING_DATE = LocalDate.of(2026, 8, 18);
	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	@Autowired
	private StrategyEvaluationRecorder recorder;

	@Autowired
	private ValuationSnapshotRepository valuationSnapshotRepository;

	@Test
	void evaluatesAndRecordsValuationSnapshots() {
		List<StrategyEvaluationResult> results = recorder.evaluateAndRecord(
				TRADING_DATE,
				List.of(
						input("NVDA", "180.0000", "6.0000", "30.0000"),
						input("GOOGL", "200.0000", "4.0000", "50.0000"),
						input("AAPL", "160.0000", "4.0000", "40.0000"),
						input("AMZN", "180.0000", "4.0000", "45.0000"),
						input("MSFT", "140.0000", "4.0000", "35.0000")
				),
				Map.of("NVDA", StrategyStage.NONE),
				StrategyRuleConfig.peMeanReversionV1(),
				STRATEGY_VERSION
		);

		assertThat(results).hasSize(5);

		List<ValuationSnapshot> snapshots = valuationSnapshotRepository
				.findByTradingDateAndStrategyVersionOrderByTicker(TRADING_DATE, STRATEGY_VERSION);

		assertThat(snapshots).hasSize(5);
		assertThat(snapshots)
				.extracting(ValuationSnapshot::getTicker)
				.containsExactly("AAPL", "AMZN", "GOOGL", "MSFT", "NVDA");

		ValuationSnapshot nvda = valuationSnapshotRepository
				.findByTradingDateAndTickerAndStrategyVersion(TRADING_DATE, "NVDA", STRATEGY_VERSION)
				.orElseThrow();

		assertThat(nvda.getPeerAveragePer()).isEqualByComparingTo("40.0000");
		assertThat(nvda.getPeerDiscount()).isEqualByComparingTo("-0.2500");
	}

	@Test
	void replacesExistingSnapshotsForSameDateAndStrategy() {
		recorder.evaluateAndRecord(
				TRADING_DATE,
				List.of(
						input("NVDA", "180.0000", "6.0000", "30.0000"),
						input("GOOGL", "200.0000", "4.0000", "50.0000"),
						input("AAPL", "160.0000", "4.0000", "40.0000"),
						input("AMZN", "180.0000", "4.0000", "45.0000"),
						input("MSFT", "140.0000", "4.0000", "35.0000")
				),
				Map.of(),
				StrategyRuleConfig.peMeanReversionV1(),
				STRATEGY_VERSION
		);

		recorder.evaluateAndRecord(
				TRADING_DATE,
				List.of(
						input("NVDA", "240.0000", "6.0000", "40.0000"),
						input("GOOGL", "200.0000", "4.0000", "50.0000"),
						input("AAPL", "160.0000", "4.0000", "40.0000"),
						input("AMZN", "180.0000", "4.0000", "45.0000"),
						input("MSFT", "140.0000", "4.0000", "35.0000")
				),
				Map.of(),
				StrategyRuleConfig.peMeanReversionV1(),
				STRATEGY_VERSION
		);

		List<ValuationSnapshot> snapshots = valuationSnapshotRepository
				.findByTradingDateAndStrategyVersionOrderByTicker(TRADING_DATE, STRATEGY_VERSION);

		assertThat(snapshots).hasSize(5);
		assertThat(valuationSnapshotRepository
				.findByTradingDateAndTickerAndStrategyVersion(TRADING_DATE, "NVDA", STRATEGY_VERSION)
				.orElseThrow()
				.getCurrentPer()).isEqualByComparingTo("40.0000");
	}

	private StrategyValuationInput input(String ticker, String closePrice, String ttmEps, String currentPer) {
		return new StrategyValuationInput(
				ticker,
				new BigDecimal(closePrice),
				new BigDecimal(ttmEps),
				new BigDecimal(currentPer)
		);
	}
}

