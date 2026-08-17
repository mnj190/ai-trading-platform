package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisOverseasOrderClient;
import com.mnj190.aitrading.broker.KisOverseasOrderRequest;
import com.mnj190.aitrading.broker.KisOverseasOrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class OrderSubmissionService {

	private final OrderHistoryRepository orderHistoryRepository;
	private final KisOverseasOrderClient kisOverseasOrderClient;

	public OrderSubmissionService(
			OrderHistoryRepository orderHistoryRepository,
			KisOverseasOrderClient kisOverseasOrderClient
	) {
		this.orderHistoryRepository = Objects.requireNonNull(orderHistoryRepository);
		this.kisOverseasOrderClient = Objects.requireNonNull(kisOverseasOrderClient);
	}

	@Transactional
	public OrderHistory submit(OrderSubmissionCommand command) {
		Objects.requireNonNull(command, "command must not be null");

		OrderHistory order = orderHistoryRepository.findById(command.orderId())
				.orElseThrow(() -> new IllegalArgumentException("order not found: " + command.orderId()));

		if (order.getStatus() != OrderStatus.REQUESTED) {
			throw new IllegalStateException("only REQUESTED orders can be submitted");
		}

		KisOverseasOrderResponse response = kisOverseasOrderClient.placeOrder(
				command.accessToken(),
				order.getSide(),
				KisOverseasOrderRequest.usLimitOrder(
						order.getTicker(),
						command.orderQuantity(),
						command.limitPrice()
				)
		);

		if (!response.isSuccess()) {
			order.markRejected();
			return orderHistoryRepository.saveAndFlush(order);
		}

		String brokerOrderId = response.brokerOrderId()
				.orElseThrow(() -> new IllegalStateException("KIS order response does not contain broker order id"));
		order.markSubmitted(brokerOrderId);
		return orderHistoryRepository.saveAndFlush(order);
	}
}
