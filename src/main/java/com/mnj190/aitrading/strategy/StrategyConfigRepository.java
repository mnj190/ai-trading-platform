package com.mnj190.aitrading.strategy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StrategyConfigRepository extends JpaRepository<StrategyConfig, Long> {

	Optional<StrategyConfig> findByStrategyVersion(String strategyVersion);

	Optional<StrategyConfig> findByStrategyVersionAndEnabledTrue(String strategyVersion);
}

