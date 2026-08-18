package com.mnj190.aitrading.market;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class PerNormalizationBaselineRepositoryTests {

	@Autowired
	private PerNormalizationBaselineRepository repository;

	@Test
	void savesFindsAndDeletesPerNormalizationBaseline() {
		LocalDate baseMonth = LocalDate.of(2026, 8, 1);
		String ticker = "TEST_NVDA";
		PerNormalizationBaseline baseline = new PerNormalizationBaseline(
				ticker,
				baseMonth,
				new BigDecimal("37.5000"),
				60,
				OffsetDateTime.now()
		);

		PerNormalizationBaseline saved = repository.saveAndFlush(baseline);

		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByTickerAndBaseMonth(ticker, baseMonth)).isPresent();
		assertThat(saved.getFiveYearAveragePer()).isEqualByComparingTo("37.5000");

		repository.delete(saved);
		repository.flush();

		assertThat(repository.existsById(saved.getId())).isFalse();
	}

	@Test
	void findsMostRecentPriorBaselineWhenExactMonthIsMissing() {
		String ticker = "TEST_CARRYFWD";
		PerNormalizationBaseline july = repository.saveAndFlush(new PerNormalizationBaseline(
				ticker,
				LocalDate.of(2026, 7, 1),
				new BigDecimal("30.0000"),
				5,
				OffsetDateTime.now()
		));
		repository.saveAndFlush(new PerNormalizationBaseline(
				ticker,
				LocalDate.of(2026, 6, 1),
				new BigDecimal("25.0000"),
				5,
				OffsetDateTime.now()
		));

		Optional<PerNormalizationBaseline> found = repository
				.findFirstByTickerAndBaseMonthLessThanEqualOrderByBaseMonthDesc(ticker, LocalDate.of(2026, 8, 1));

		assertThat(found).isPresent();
		assertThat(found.get().getId()).isEqualTo(july.getId());
		assertThat(found.get().getFiveYearAveragePer()).isEqualByComparingTo("30.0000");
	}
}
