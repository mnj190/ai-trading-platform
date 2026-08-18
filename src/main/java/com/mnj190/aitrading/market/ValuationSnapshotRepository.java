package com.mnj190.aitrading.market;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	Optional<ValuationSnapshot> findFirstByTickerAndStrategyVersionOrderByTradingDateDesc(
			String ticker,
			String strategyVersion
	);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("""
			delete from ValuationSnapshot snapshot
			where snapshot.tradingDate = :tradingDate
			and snapshot.strategyVersion = :strategyVersion
			""")
	int deleteByTradingDateAndStrategyVersion(
			@Param("tradingDate") LocalDate tradingDate,
			@Param("strategyVersion") String strategyVersion
	);
}
