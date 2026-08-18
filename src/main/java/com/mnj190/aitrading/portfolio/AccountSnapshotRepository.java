package com.mnj190.aitrading.portfolio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, Long> {

	Optional<AccountSnapshot> findBySnapshotDate(LocalDate snapshotDate);
}
