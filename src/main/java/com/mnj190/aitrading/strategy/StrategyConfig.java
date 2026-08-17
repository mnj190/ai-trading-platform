package com.mnj190.aitrading.strategy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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

	@Column(name = "buy1_threshold", nullable = false, precision = 8, scale = 4)
	private BigDecimal buy1Threshold;

	@Column(name = "buy2_threshold", nullable = false, precision = 8, scale = 4)
	private BigDecimal buy2Threshold;

	@Column(name = "buy3_threshold", nullable = false, precision = 8, scale = 4)
	private BigDecimal buy3Threshold;

	@Column(name = "buy_unit_ratio", nullable = false, precision = 8, scale = 4)
	private BigDecimal buyUnitRatio;

	@Column(name = "sell_threshold", nullable = false, precision = 8, scale = 4)
	private BigDecimal sellThreshold;

	@Column(nullable = false)
	private boolean enabled;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected StrategyConfig() {
	}

	public StrategyConfig(
			String strategyVersion,
			BigDecimal buy1Threshold,
			BigDecimal buy2Threshold,
			BigDecimal buy3Threshold,
			BigDecimal buyUnitRatio,
			BigDecimal sellThreshold,
			boolean enabled
	) {
		this.strategyVersion = strategyVersion;
		this.buy1Threshold = buy1Threshold;
		this.buy2Threshold = buy2Threshold;
		this.buy3Threshold = buy3Threshold;
		this.buyUnitRatio = buyUnitRatio;
		this.sellThreshold = sellThreshold;
		this.enabled = enabled;
	}

	@PrePersist
	void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getStrategyVersion() {
		return strategyVersion;
	}

	public BigDecimal getBuy1Threshold() {
		return buy1Threshold;
	}

	public BigDecimal getBuy2Threshold() {
		return buy2Threshold;
	}

	public BigDecimal getBuy3Threshold() {
		return buy3Threshold;
	}

	public BigDecimal getBuyUnitRatio() {
		return buyUnitRatio;
	}

	public BigDecimal getSellThreshold() {
		return sellThreshold;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void disable() {
		this.enabled = false;
	}
}

