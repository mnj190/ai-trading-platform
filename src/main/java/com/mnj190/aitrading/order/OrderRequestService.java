package com.mnj190.aitrading.order;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class OrderRequestService {

	private final OrderCandidateFactory orderCandidateFactory;
	private final OrderHistoryRepository orderHistoryRepository;

	public OrderRequestService(
			OrderCandidateFactory orderCandidateFactory,
			OrderHistoryRepository orderHistoryRepository
	) {
		this.orderCandidateFactory = Objects.requireNonNull(orderCandidateFactory);
		this.orderHistoryRepository = Objects.requireNonNull(orderHistoryRepository);
	}

	@Transactional
	public List<OrderHistory> createRequestedOrders(OrderRequestCommand command) {
		Objects.requireNonNull(command, "command must not be null");

		List<OrderHistory> orders = orderCandidateFactory.create(
						command.decision(),
						command.availableCash(),
						command.currentHoldingQuantity(),
						command.currentHoldingMarketValue(),
						command.strategyVersion()
				).stream()
				.map(candidate -> OrderHistory.fromCandidate(candidate, command.orderedAt()))
				.toList();

		return orderHistoryRepository.saveAllAndFlush(orders);
	}
}
