package com.mnj190.aitrading.market;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class ValuationSnapshotRepositoryTests {

	private static final LocalDate TRADING_DATE = LocalDate.of(2026, 8, 17);
	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	@Autowired
	private ValuationSnapshotRepository repository;

	@Test
	void savesFindsAndDeletesValuationSnapshot() {
		ValuationSnapshot snapshot = new ValuationSnapshot(
				TRADING_DATE,
				"NVDA",
				new BigDecimal("180.0000"),
				new BigDecimal("4.0000"),
				new BigDecimal("45.0000"),
				new BigDecimal("60.0000"),
				new BigDecimal("0.7500"),
				new BigDecimal("1.0000"),
				new BigDecimal("-0.1000"),
				STRATEGY_VERSION
		);

		ValuationSnapshot saved = repository.saveAndFlush(snapshot);

		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByTradingDateAndTickerAndStrategyVersion(
				TRADING_DATE,
				"NVDA",
				STRATEGY_VERSION
		)).isPresent();

		List<ValuationSnapshot> snapshots = repository.findByTradingDateAndStrategyVersionOrderByTicker(
				TRADING_DATE,
				STRATEGY_VERSION
		);

		assertThat(snapshots)
				.extracting(ValuationSnapshot::getTicker)
				.contains("NVDA");

		repository.delete(saved);
		repository.flush();

		assertThat(repository.existsById(saved.getId())).isFalse();
	}

	@Test
	void findsMostRecentSnapshotForTickerAcrossTradingDates() {
		String ticker = "TEST_RECENT";
		repository.saveAndFlush(new ValuationSnapshot(
				LocalDate.of(2026, 8, 14),
				ticker,
				new BigDecimal("170.0000"),
				new BigDecimal("4.0000"),
				new BigDecimal("42.0000"),
				new BigDecimal("60.0000"),
				new BigDecimal("0.7000"),
				new BigDecimal("1.0000"),
				new BigDecimal("-0.3000"),
				STRATEGY_VERSION
		));
		ValuationSnapshot latest = repository.saveAndFlush(new ValuationSnapshot(
				LocalDate.of(2026, 8, 17),
				ticker,
				new BigDecimal("180.0000"),
				new BigDecimal("4.0000"),
				new BigDecimal("45.0000"),
				new BigDecimal("60.0000"),
				new BigDecimal("0.7500"),
				new BigDecimal("1.0500"),
				new BigDecimal("-0.1000"),
				STRATEGY_VERSION
		));

		var found = repository.findFirstByTickerAndStrategyVersionOrderByTradingDateDesc(ticker, STRATEGY_VERSION);

		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(latest.getId());
		assertThat(found.get().getTradingDate()).isEqualTo(LocalDate.of(2026, 8, 17));
	}
}
