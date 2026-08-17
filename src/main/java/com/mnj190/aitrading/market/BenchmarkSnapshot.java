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
@Table(name = "benchmark_snapshot", schema = "trading")
public class BenchmarkSnapshot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "benchmark_symbol", nullable = false, length = 16)
	private String benchmarkSymbol;

	@Column(name = "snapshot_date", nullable = false)
	private LocalDate snapshotDate;

	@Column(name = "close_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal closePrice;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected BenchmarkSnapshot() {
	}

	public BenchmarkSnapshot(String benchmarkSymbol, LocalDate snapshotDate, BigDecimal closePrice) {
		this.benchmarkSymbol = benchmarkSymbol;
		this.snapshotDate = snapshotDate;
		this.closePrice = closePrice;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getBenchmarkSymbol() {
		return benchmarkSymbol;
	}

	public LocalDate getSnapshotDate() {
		return snapshotDate;
	}

	public BigDecimal getClosePrice() {
		return closePrice;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
