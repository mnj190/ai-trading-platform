package com.mnj190.aitrading.portfolio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "account_snapshot", schema = "trading")
public class AccountSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "snapshot_date", nullable = false, unique = true)
	private LocalDate snapshotDate;

	@Column(name = "total_equity", nullable = false, precision = 19, scale = 4)
	private BigDecimal totalEquity;

	@Column(name = "cash_balance", nullable = false, precision = 19, scale = 4)
	private BigDecimal cashBalance;

	@Column(name = "stock_market_value", nullable = false, precision = 19, scale = 4)
	private BigDecimal stockMarketValue;

	@Column(name = "unrealized_pnl", nullable = false, precision = 19, scale = 4)
	private BigDecimal unrealizedPnl;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected AccountSnapshot() {
	}

	public AccountSnapshot(
			LocalDate snapshotDate,
			BigDecimal totalEquity,
			BigDecimal cashBalance,
			BigDecimal stockMarketValue,
			BigDecimal unrealizedPnl,
			String currency
	) {
		this.snapshotDate = snapshotDate;
		this.totalEquity = totalEquity;
		this.cashBalance = cashBalance;
		this.stockMarketValue = stockMarketValue;
		this.unrealizedPnl = unrealizedPnl;
		this.currency = currency;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public LocalDate getSnapshotDate() {
		return snapshotDate;
	}

	public BigDecimal getTotalEquity() {
		return totalEquity;
	}

	public BigDecimal getCashBalance() {
		return cashBalance;
	}

	public BigDecimal getStockMarketValue() {
		return stockMarketValue;
	}

	public BigDecimal getUnrealizedPnl() {
		return unrealizedPnl;
	}

	public String getCurrency() {
		return currency;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
