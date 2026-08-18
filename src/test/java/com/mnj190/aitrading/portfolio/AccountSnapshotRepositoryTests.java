package com.mnj190.aitrading.portfolio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class AccountSnapshotRepositoryTests {

	@Autowired
	private AccountSnapshotRepository repository;

	@Test
	void savesFindsAndDeletesAccountSnapshot() {
		LocalDate snapshotDate = LocalDate.of(2026, 8, 17);
		AccountSnapshot snapshot = new AccountSnapshot(
				snapshotDate,
				new BigDecimal("1000.0000"),
				new BigDecimal("200.0000"),
				new BigDecimal("800.0000"),
				new BigDecimal("50.0000"),
				"USD"
		);

		AccountSnapshot saved = repository.saveAndFlush(snapshot);

		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findBySnapshotDate(snapshotDate)).isPresent();
		assertThat(saved.getTotalEquity()).isEqualByComparingTo("1000.0000");
		assertThat(saved.getCashBalance()).isEqualByComparingTo("200.0000");
		assertThat(saved.getStockMarketValue()).isEqualByComparingTo("800.0000");
		assertThat(saved.getUnrealizedPnl()).isEqualByComparingTo("50.0000");
		assertThat(saved.getCurrency()).isEqualTo("USD");

		repository.delete(saved);
		repository.flush();

		assertThat(repository.existsById(saved.getId())).isFalse();
	}
}
