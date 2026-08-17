package com.mnj190.aitrading.strategy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("local")
@SpringBootTest
@Transactional
class StrategyConfigRepositoryTests {

	@Autowired
	private StrategyConfigRepository repository;

	@Test
	void findsInitialStrategyConfig() {
		StrategyConfig config = repository
				.findByStrategyVersionAndEnabledTrue("PE_MEAN_REVERSION_V1")
				.orElseThrow();

		assertThat(config.getBuy1Threshold()).isEqualByComparingTo("-0.1500");
		assertThat(config.getBuy2Threshold()).isEqualByComparingTo("-0.2000");
		assertThat(config.getBuy3Threshold()).isEqualByComparingTo("-0.2500");
		assertThat(config.getBuyUnitRatio()).isEqualByComparingTo("0.1000");
		assertThat(config.getSellThreshold()).isEqualByComparingTo("0.0000");
	}

	@Test
	void savesUpdatesAndDeletesStrategyConfig() {
		StrategyConfig config = new StrategyConfig(
				"TEST_STRATEGY",
				new BigDecimal("-0.1000"),
				new BigDecimal("-0.2000"),
				new BigDecimal("-0.3000"),
				new BigDecimal("0.1000"),
				new BigDecimal("0.0000"),
				true
		);

		StrategyConfig saved = repository.saveAndFlush(config);

		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByStrategyVersion("TEST_STRATEGY")).isPresent();

		saved.disable();
		repository.saveAndFlush(saved);

		assertThat(repository.findByStrategyVersionAndEnabledTrue("TEST_STRATEGY")).isEmpty();

		repository.delete(saved);
		repository.flush();

		assertThat(repository.existsById(saved.getId())).isFalse();
	}
}

