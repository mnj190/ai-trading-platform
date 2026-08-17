package com.mnj190.aitrading.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

	List<TradeHistory> findByOrder_IdOrderByExecutedAtAsc(Long orderId);
}
