package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisOrderExecutionFill;
import com.mnj190.aitrading.broker.KisOrderExecutionFillMapper;
import com.mnj190.aitrading.broker.KisOverseasOrderExecutionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class KisOrderExecutionSyncService {

	private final KisOrderExecutionFillMapper fillMapper;
	private final OrderHistoryRepository orderHistoryRepository;
	private final TradeExecutionService tradeExecutionService;

	public KisOrderExecutionSyncService(
			KisOrderExecutionFillMapper fillMapper,
			OrderHistoryRepository orderHistoryRepository,
			TradeExecutionService tradeExecutionService
	) {
		this.fillMapper = Objects.requireNonNull(fillMapper);
		this.orderHistoryRepository = Objects.requireNonNull(orderHistoryRepository);
		this.tradeExecutionService = Objects.requireNonNull(tradeExecutionService);
	}

	@Transactional
	public ExecutionSyncReport sync(KisOverseasOrderExecutionResponse response, OffsetDateTime fallbackExecutedAt) {
		Objects.requireNonNull(response, "response must not be null");
		Objects.requireNonNull(fallbackExecutedAt, "fallbackExecutedAt must not be null");

		List<KisOrderExecutionFill> fills = fillMapper.toFills(response, fallbackExecutedAt);
		int recorded = 0;
		int unknown = 0;
		int nonSubmitted = 0;
		int alreadyUpToDate = 0;

		for (KisOrderExecutionFill fill : fills) {
			OrderHistory order = orderHistoryRepository.findByBrokerOrderId(fill.brokerOrderId())
					.orElse(null);
			if (order == null) {
				unknown++;
				continue;
			}
			if (order.getStatus() != OrderStatus.SUBMITTED && order.getStatus() != OrderStatus.PARTIALLY_FILLED) {
				nonSubmitted++;
				continue;
			}
			Optional<TradeHistory> trade = tradeExecutionService.recordFill(new TradeExecutionCommand(
					order.getId(),
					fill.cumulativeExecutedQuantity(),
					fill.executedPrice(),
					fill.executedAt()
			));
			if (trade.isPresent()) {
				recorded++;
			}
			else {
				alreadyUpToDate++;
			}
		}

		return new ExecutionSyncReport(fills.size(), recorded, unknown, nonSubmitted, alreadyUpToDate);
	}
}
