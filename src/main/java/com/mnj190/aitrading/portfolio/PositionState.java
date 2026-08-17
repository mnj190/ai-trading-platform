package com.mnj190.aitrading.portfolio;

import com.mnj190.aitrading.strategy.StrategyStage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "position_state", schema = "trading")
public class PositionState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 16)
	private String ticker;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private StrategyStage state;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal quantity;

	@Column(name = "invested_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal investedAmount;

	@Column(name = "strategy_version", nullable = false, length = 64)
	private String strategyVersion;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected PositionState() {
	}

	public PositionState(
			String ticker,
			StrategyStage state,
			BigDecimal quantity,
			BigDecimal investedAmount,
			String strategyVersion
	) {
		this.ticker = ticker;
		this.state = state;
		this.quantity = quantity;
		this.investedAmount = investedAmount;
		this.strategyVersion = strategyVersion;
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

	public String getTicker() {
		return ticker;
	}

	public StrategyStage getState() {
		return state;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getInvestedAmount() {
		return investedAmount;
	}

	public String getStrategyVersion() {
		return strategyVersion;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void updateHolding(StrategyStage state, BigDecimal quantity, BigDecimal investedAmount) {
		this.state = state;
		this.quantity = quantity;
		this.investedAmount = investedAmount;
	}
}

