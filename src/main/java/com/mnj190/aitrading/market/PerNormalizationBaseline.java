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
@Table(name = "per_normalization_baseline", schema = "trading")
public class PerNormalizationBaseline {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 16)
	private String ticker;

	@Column(name = "base_month", nullable = false)
	private LocalDate baseMonth;

	@Column(name = "five_year_average_per", nullable = false, precision = 12, scale = 4)
	private BigDecimal fiveYearAveragePer;

	@Column(name = "sample_count", nullable = false)
	private int sampleCount;

	@Column(name = "calculated_at", nullable = false)
	private OffsetDateTime calculatedAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected PerNormalizationBaseline() {
	}

	public PerNormalizationBaseline(
			String ticker,
			LocalDate baseMonth,
			BigDecimal fiveYearAveragePer,
			int sampleCount,
			OffsetDateTime calculatedAt
	) {
		this.ticker = ticker;
		this.baseMonth = baseMonth;
		this.fiveYearAveragePer = fiveYearAveragePer;
		this.sampleCount = sampleCount;
		this.calculatedAt = calculatedAt;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getTicker() {
		return ticker;
	}

	public LocalDate getBaseMonth() {
		return baseMonth;
	}

	public BigDecimal getFiveYearAveragePer() {
		return fiveYearAveragePer;
	}

	public int getSampleCount() {
		return sampleCount;
	}

	public OffsetDateTime getCalculatedAt() {
		return calculatedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
