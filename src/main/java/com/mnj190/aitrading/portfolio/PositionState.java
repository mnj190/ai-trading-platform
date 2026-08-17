package com.mnj190.aitrading.portfolio;

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
	private PositionStatus status;

	@Column(nullable = false, precision = 19, scale = 6)
	private BigDecimal quantity;

	@Column(name = "average_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal averagePrice;

	@Column(name = "invested_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal investedAmount;

	@Column(name = "strategy_version", nullable = false, unique = true, length = 64)
	private String strategyVersion;

	@Column(name = "opened_at", nullable = false)
	private OffsetDateTime openedAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected PositionState() {
	}

	public PositionState(
			String ticker,
			BigDecimal quantity,
			BigDecimal averagePrice,
			BigDecimal investedAmount,
			String strategyVersion
	) {
		this.ticker = ticker;
		this.status = PositionStatus.HOLDING;
		this.quantity = quantity;
		this.averagePrice = averagePrice;
		this.investedAmount = investedAmount;
		this.strategyVersion = strategyVersion;
	}

	@PrePersist
	void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		this.openedAt = now;
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

	public PositionStatus getStatus() {
		return status;
	}

	public BigDecimal getQuantity() {
		return quantity;
	}

	public BigDecimal getAveragePrice() {
		return averagePrice;
	}

	public BigDecimal getInvestedAmount() {
		return investedAmount;
	}

	public String getStrategyVersion() {
		return strategyVersion;
	}

	public OffsetDateTime getOpenedAt() {
		return openedAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void updateHolding(String ticker, BigDecimal quantity, BigDecimal averagePrice, BigDecimal investedAmount) {
		this.ticker = ticker;
		this.quantity = quantity;
		this.averagePrice = averagePrice;
		this.investedAmount = investedAmount;
	}
}

