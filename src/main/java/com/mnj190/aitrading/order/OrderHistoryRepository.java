package com.mnj190.aitrading.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

	List<OrderHistory> findByStrategyVersionOrderByOrderedAtAsc(String strategyVersion);

	Optional<OrderHistory> findByBrokerOrderId(String brokerOrderId);
}
