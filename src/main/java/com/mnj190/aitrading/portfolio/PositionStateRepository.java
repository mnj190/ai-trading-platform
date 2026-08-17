package com.mnj190.aitrading.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PositionStateRepository extends JpaRepository<PositionState, Long> {

	Optional<PositionState> findByTickerAndStrategyVersion(String ticker, String strategyVersion);

	Optional<PositionState> findByStrategyVersion(String strategyVersion);
}
