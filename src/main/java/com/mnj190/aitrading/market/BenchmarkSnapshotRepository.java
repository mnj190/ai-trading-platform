package com.mnj190.aitrading.market;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface BenchmarkSnapshotRepository extends JpaRepository<BenchmarkSnapshot, Long> {

	Optional<BenchmarkSnapshot> findByBenchmarkSymbolAndSnapshotDate(String benchmarkSymbol, LocalDate snapshotDate);
}
