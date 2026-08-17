package com.mnj190.aitrading.strategy;

import com.mnj190.aitrading.order.OrderHistory;
import com.mnj190.aitrading.order.OrderRequestCommand;
import com.mnj190.aitrading.order.OrderRequestService;
import com.mnj190.aitrading.portfolio.PositionState;
import com.mnj190.aitrading.portfolio.PositionStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DailyEvaluationService {

	private final StrategyConfigRepository strategyConfigRepository;
	private final PositionStateRepository positionStateRepository;
	private final StrategyEvaluationRecorder evaluationRecorder;
	private final OrderRequestService orderRequestService;

	public DailyEvaluationService(
			StrategyConfigRepository strategyConfigRepository,
			PositionStateRepository positionStateRepository,
			StrategyEvaluationRecorder evaluationRecorder,
			OrderRequestService orderRequestService
	) {
		this.strategyConfigRepository = Objects.requireNonNull(strategyConfigRepository);
		this.positionStateRepository = Objects.requireNonNull(positionStateRepository);
		this.evaluationRecorder = Objects.requireNonNull(evaluationRecorder);
		this.orderRequestService = Objects.requireNonNull(orderRequestService);
	}

	@Transactional
	public DailyEvaluationReport evaluateAndCreateOrderRequests(DailyEvaluationCommand command) {
		Objects.requireNonNull(command, "command must not be null");

		StrategyConfig strategyConfig = strategyConfigRepository
				.findByStrategyVersionAndEnabledTrue(command.strategyVersion())
				.orElseThrow(() -> new IllegalStateException(
						"enabled strategy config not found: " + command.strategyVersion()
				));
		StrategyRuleConfig ruleConfig = toRuleConfig(strategyConfig);
		Optional<PositionState> currentPosition = positionStateRepository
				.findByStrategyVersion(command.strategyVersion());
		Optional<String> currentHoldingTicker = currentPosition.map(PositionState::getTicker);

		StrategyEvaluation evaluation = evaluationRecorder.evaluateRecordAndReturn(
				command.tradingDate(),
				command.valuationInputs(),
				currentHoldingTicker,
				ruleConfig,
				command.strategyVersion()
		);

		BigDecimal currentHoldingQuantity = currentPosition
				.map(PositionState::getQuantity)
				.orElse(null);
		BigDecimal currentHoldingMarketValue = currentPosition
				.map(position -> calculateMarketValue(position, command.valuationInputs()))
				.orElse(BigDecimal.ZERO);

		List<OrderHistory> requestedOrders = orderRequestService.createRequestedOrders(new OrderRequestCommand(
				evaluation.decision(),
				command.availableCash(),
				currentHoldingQuantity,
				currentHoldingMarketValue,
				command.strategyVersion(),
				command.evaluatedAt()
		));

		return new DailyEvaluationReport(evaluation, currentHoldingTicker, requestedOrders);
	}

	private StrategyRuleConfig toRuleConfig(StrategyConfig strategyConfig) {
		return new StrategyRuleConfig(
				strategyConfig.getEntryThreshold(),
				strategyConfig.getSwitchThreshold(),
				strategyConfig.getExitThreshold(),
				strategyConfig.getMaxPositions()
		);
	}

	private BigDecimal calculateMarketValue(PositionState position, List<StrategyValuationInput> valuationInputs) {
		Map<String, StrategyValuationInput> inputsByTicker = valuationInputs.stream()
				.collect(Collectors.toMap(StrategyValuationInput::ticker, Function.identity()));
		StrategyValuationInput input = Optional.ofNullable(inputsByTicker.get(position.getTicker()))
				.orElseThrow(() -> new IllegalStateException(
						"missing valuation input for current holding: " + position.getTicker()
				));
		return position.getQuantity().multiply(input.closePrice());
	}
}
