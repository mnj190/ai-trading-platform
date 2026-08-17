package com.mnj190.aitrading.market;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ValuationSnapshotRepository extends JpaRepository<ValuationSnapshot, Long> {

	List<ValuationSnapshot> findByTradingDateAndStrategyVersionOrderByTicker(
			LocalDate tradingDate,
			String strategyVersion
	);

	Optional<ValuationSnapshot> findByTradingDateAndTickerAndStrategyVersion(
			LocalDate tradingDate,
			String ticker,
			String strategyVersion
	);
}

