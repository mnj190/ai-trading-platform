package com.mnj190.aitrading.portfolio;

import com.mnj190.aitrading.strategy.StrategyStage;
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
class PositionStateRepositoryTests {

	private static final String STRATEGY_VERSION = "PE_MEAN_REVERSION_V1";

	@Autowired
	private PositionStateRepository repository;

	@Test
	void savesFindsUpdatesAndDeletesPositionState() {
		PositionState position = new PositionState(
				"NVDA",
				StrategyStage.NONE,
				new BigDecimal("0.000000"),
				new BigDecimal("0.0000"),
				STRATEGY_VERSION
		);

		PositionState saved = repository.saveAndFlush(position);

		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByTickerAndStrategyVersion("NVDA", STRATEGY_VERSION)).isPresent();

		saved.updateHolding(
				StrategyStage.BUY1,
				new BigDecimal("1.000000"),
				new BigDecimal("200.0000")
		);
		repository.saveAndFlush(saved);

		PositionState updated = repository
				.findByTickerAndStrategyVersion("NVDA", STRATEGY_VERSION)
				.orElseThrow();

		assertThat(updated.getState()).isEqualTo(StrategyStage.BUY1);
		assertThat(updated.getQuantity()).isEqualByComparingTo("1.000000");
		assertThat(updated.getInvestedAmount()).isEqualByComparingTo("200.0000");

		repository.delete(updated);
		repository.flush();

		assertThat(repository.existsById(updated.getId())).isFalse();
	}
}

