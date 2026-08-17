package com.mnj190.aitrading.order;

import com.mnj190.aitrading.portfolio.PositionState;
import com.mnj190.aitrading.portfolio.PositionStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
public class TradeExecutionService {

	private static final int MONEY_SCALE = 4;
	private static final int QUANTITY_SCALE = 6;

	private final OrderHistoryRepository orderHistoryRepository;
	private final TradeHistoryRepository tradeHistoryRepository;
	private final PositionStateRepository positionStateRepository;

	public TradeExecutionService(
			OrderHistoryRepository orderHistoryRepository,
			TradeHistoryRepository tradeHistoryRepository,
			PositionStateRepository positionStateRepository
	) {
		this.orderHistoryRepository = Objects.requireNonNull(orderHistoryRepository);
		this.tradeHistoryRepository = Objects.requireNonNull(tradeHistoryRepository);
		this.positionStateRepository = Objects.requireNonNull(positionStateRepository);
	}

	@Transactional
	public TradeHistory recordFill(TradeExecutionCommand command) {
		Objects.requireNonNull(command, "command must not be null");

		OrderHistory order = orderHistoryRepository.findById(command.orderId())
				.orElseThrow(() -> new IllegalArgumentException("order not found: " + command.orderId()));
		if (order.getStatus() != OrderStatus.SUBMITTED) {
			throw new IllegalStateException("only SUBMITTED orders can be filled");
		}

		BigDecimal executedAmount = amount(command.executedQuantity(), command.executedPrice());
		TradeHistory trade = tradeHistoryRepository.saveAndFlush(new TradeHistory(
				order,
				order.getTicker(),
				order.getSide(),
				order.getOrderReason(),
				command.executedQuantity(),
				command.executedPrice(),
				executedAmount,
				command.executedAt()
		));

		if (order.getSide() == OrderSide.BUY) {
			applyBuy(order, command.executedQuantity(), executedAmount);
		}
		else {
			applySell(order, command.executedQuantity());
		}

		order.markFilled();
		orderHistoryRepository.saveAndFlush(order);
		return trade;
	}

	private void applyBuy(OrderHistory order, BigDecimal executedQuantity, BigDecimal executedAmount) {
		positionStateRepository.findByStrategyVersion(order.getStrategyVersion())
				.ifPresentOrElse(
						position -> updateExistingBuy(position, order, executedQuantity, executedAmount),
						() -> positionStateRepository.saveAndFlush(new PositionState(
								order.getTicker(),
								executedQuantity,
								averagePrice(executedAmount, executedQuantity),
								executedAmount,
								order.getStrategyVersion()
						))
				);
	}

	private void updateExistingBuy(
			PositionState position,
			OrderHistory order,
			BigDecimal executedQuantity,
			BigDecimal executedAmount
	) {
		if (!position.getTicker().equals(order.getTicker())) {
			throw new IllegalStateException(
					"cannot add BUY fill for " + order.getTicker() + " while holding " + position.getTicker()
			);
		}
		BigDecimal newQuantity = quantity(position.getQuantity().add(executedQuantity));
		BigDecimal newInvestedAmount = amount(position.getInvestedAmount().add(executedAmount));
		position.updateHolding(
				position.getTicker(),
				newQuantity,
				averagePrice(newInvestedAmount, newQuantity),
				newInvestedAmount
		);
		positionStateRepository.saveAndFlush(position);
	}

	private void applySell(OrderHistory order, BigDecimal executedQuantity) {
		PositionState position = positionStateRepository.findByStrategyVersion(order.getStrategyVersion())
				.orElseThrow(() -> new IllegalStateException("position not found for " + order.getStrategyVersion()));
		if (!position.getTicker().equals(order.getTicker())) {
			throw new IllegalStateException(
					"cannot sell " + order.getTicker() + " while holding " + position.getTicker()
			);
		}
		if (executedQuantity.compareTo(position.getQuantity()) > 0) {
			throw new IllegalStateException("executed sell quantity exceeds current position quantity");
		}
		if (executedQuantity.compareTo(position.getQuantity()) == 0) {
			positionStateRepository.delete(position);
			positionStateRepository.flush();
			return;
		}

		BigDecimal remainingQuantity = quantity(position.getQuantity().subtract(executedQuantity));
		position.updateHolding(
				position.getTicker(),
				remainingQuantity,
				position.getAveragePrice(),
				amount(position.getAveragePrice().multiply(remainingQuantity))
		);
		positionStateRepository.saveAndFlush(position);
	}

	private BigDecimal amount(BigDecimal value) {
		return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
	}

	private BigDecimal amount(BigDecimal quantity, BigDecimal price) {
		return amount(quantity.multiply(price));
	}

	private BigDecimal quantity(BigDecimal value) {
		return value.setScale(QUANTITY_SCALE, RoundingMode.HALF_UP);
	}

	private BigDecimal averagePrice(BigDecimal amount, BigDecimal quantity) {
		return amount.divide(quantity, MONEY_SCALE, RoundingMode.HALF_UP);
	}
}
