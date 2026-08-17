package com.mnj190.aitrading.market;

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
class BenchmarkSnapshotRepositoryTests {

	@Autowired
	private BenchmarkSnapshotRepository repository;

	@Test
	void savesFindsAndDeletesBenchmarkSnapshot() {
		LocalDate snapshotDate = LocalDate.of(2026, 8, 17);
		BenchmarkSnapshot snapshot = new BenchmarkSnapshot(
				"SPY",
				snapshotDate,
				new BigDecimal("640.0000")
		);

		BenchmarkSnapshot saved = repository.saveAndFlush(snapshot);

		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByBenchmarkSymbolAndSnapshotDate("SPY", snapshotDate)).isPresent();
		assertThat(saved.getClosePrice()).isEqualByComparingTo("640.0000");

		repository.delete(saved);
		repository.flush();

		assertThat(repository.existsById(saved.getId())).isFalse();
	}
}
