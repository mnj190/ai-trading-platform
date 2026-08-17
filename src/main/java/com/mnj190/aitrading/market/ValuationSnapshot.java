package com.mnj190.aitrading.market;

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
@Table(name = "valuation_snapshot", schema = "trading")
public class ValuationSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "trading_date", nullable = false)
	private LocalDate tradingDate;

	@Column(nullable = false, length = 16)
	private String ticker;

	@Column(name = "close_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal closePrice;

	@Column(name = "ttm_eps", nullable = false, precision = 19, scale = 4)
	private BigDecimal ttmEps;

	@Column(name = "current_per", nullable = false, precision = 12, scale = 4)
	private BigDecimal currentPer;

	@Column(name = "five_year_average_per", nullable = false, precision = 12, scale = 4)
	private BigDecimal fiveYearAveragePer;

	@Column(name = "normalized_per", nullable = false, precision = 12, scale = 4)
	private BigDecimal normalizedPer;

	@Column(name = "peer_average_normalized_per", nullable = false, precision = 12, scale = 4)
	private BigDecimal peerAverageNormalizedPer;

	@Column(name = "peer_discount", nullable = false, precision = 8, scale = 4)
	private BigDecimal peerDiscount;

	@Column(name = "strategy_version", nullable = false, length = 64)
	private String strategyVersion;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected ValuationSnapshot() {
	}

	public ValuationSnapshot(
			LocalDate tradingDate,
			String ticker,
			BigDecimal closePrice,
			BigDecimal ttmEps,
			BigDecimal currentPer,
			BigDecimal fiveYearAveragePer,
			BigDecimal normalizedPer,
			BigDecimal peerAverageNormalizedPer,
			BigDecimal peerDiscount,
			String strategyVersion
	) {
		this.tradingDate = tradingDate;
		this.ticker = ticker;
		this.closePrice = closePrice;
		this.ttmEps = ttmEps;
		this.currentPer = currentPer;
		this.fiveYearAveragePer = fiveYearAveragePer;
		this.normalizedPer = normalizedPer;
		this.peerAverageNormalizedPer = peerAverageNormalizedPer;
		this.peerDiscount = peerDiscount;
		this.strategyVersion = strategyVersion;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public LocalDate getTradingDate() {
		return tradingDate;
	}

	public String getTicker() {
		return ticker;
	}

	public BigDecimal getClosePrice() {
		return closePrice;
	}

	public BigDecimal getTtmEps() {
		return ttmEps;
	}

	public BigDecimal getCurrentPer() {
		return currentPer;
	}

	public BigDecimal getFiveYearAveragePer() {
		return fiveYearAveragePer;
	}

	public BigDecimal getNormalizedPer() {
		return normalizedPer;
	}

	public BigDecimal getPeerAverageNormalizedPer() {
		return peerAverageNormalizedPer;
	}

	public BigDecimal getPeerDiscount() {
		return peerDiscount;
	}

	public String getStrategyVersion() {
		return strategyVersion;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}

