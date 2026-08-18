package com.mnj190.aitrading.market;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PerNormalizationBaselineRepository extends JpaRepository<PerNormalizationBaseline, Long> {

	Optional<PerNormalizationBaseline> findByTickerAndBaseMonth(String ticker, LocalDate baseMonth);

	Optional<PerNormalizationBaseline> findFirstByTickerAndBaseMonthLessThanEqualOrderByBaseMonthDesc(
			String ticker,
			LocalDate baseMonth
	);
}
