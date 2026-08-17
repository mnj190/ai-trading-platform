package com.mnj190.aitrading.strategy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "strategy_config", schema = "trading")
public class StrategyConfig {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "strategy_version", nullable = false, unique = true, length = 64)
	private String strategyVersion;

	@Column(name = "entry_threshold", nullable = false, precision = 8, scale = 4)
	private BigDecimal entryThreshold;

	@Column(name = "switch_threshold", nullable = false, precision = 8, scale = 4)
	private BigDecimal switchThreshold;

	@Column(name = "exit_threshold", nullable = false, precision = 8, scale = 4)
	private BigDecimal exitThreshold;

	@Column(name = "max_positions", nullable = false)
	private int maxPositions;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected StrategyConfig() {
	}

	public StrategyConfig(
			String strategyVersion,
			BigDecimal entryThreshold,
			BigDecimal switchThreshold,
			BigDecimal exitThreshold,
			int maxPositions,
			boolean enabled
	) {
		this.strategyVersion = strategyVersion;
		this.entryThreshold = entryThreshold;
		this.switchThreshold = switchThreshold;
		this.exitThreshold = exitThreshold;
		this.maxPositions = maxPositions;
		this.enabled = enabled;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getStrategyVersion() {
		return strategyVersion;
	}

	public BigDecimal getEntryThreshold() {
		return entryThreshold;
	}

	public BigDecimal getSwitchThreshold() {
		return switchThreshold;
	}

	public BigDecimal getExitThreshold() {
		return exitThreshold;
	}

	public int getMaxPositions() {
		return maxPositions;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void disable() {
		this.enabled = false;
	}
}

