package com.mnj190.aitrading.strategy;

import com.mnj190.aitrading.market.ValuationSnapshot;
import com.mnj190.aitrading.market.ValuationSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StrategyEvaluationRecorder {

	private final ValuationSnapshotRepository valuationSnapshotRepository;
	private final StrategyEvaluationService strategyEvaluationService;

	@Autowired
	public StrategyEvaluationRecorder(ValuationSnapshotRepository valuationSnapshotRepository) {
		this(valuationSnapshotRepository, new StrategyEvaluationService());
	}

	StrategyEvaluationRecorder(
			ValuationSnapshotRepository valuationSnapshotRepository,
			StrategyEvaluationService strategyEvaluationService
	) {
		this.valuationSnapshotRepository = Objects.requireNonNull(valuationSnapshotRepository);
		this.strategyEvaluationService = Objects.requireNonNull(strategyEvaluationService);
	}

	@Transactional
	public List<StrategyEvaluationResult> evaluateAndRecord(
			LocalDate tradingDate,
			List<StrategyValuationInput> valuationInputs,
			Map<String, StrategyStage> currentStages,
			StrategyRuleConfig config,
			String strategyVersion
	) {
		Objects.requireNonNull(tradingDate, "tradingDate must not be null");
		Objects.requireNonNull(valuationInputs, "valuationInputs must not be null");
		Objects.requireNonNull(currentStages, "currentStages must not be null");
		Objects.requireNonNull(config, "config must not be null");
		if (strategyVersion == null || strategyVersion.isBlank()) {
			throw new IllegalArgumentException("strategyVersion must not be blank");
		}

		List<StrategyEvaluationResult> results = strategyEvaluationService.evaluate(
				valuationInputs.stream()
						.map(StrategyValuationInput::toPeerPerInput)
						.toList(),
				currentStages,
				config
		);

		Map<String, StrategyValuationInput> inputsByTicker = valuationInputs.stream()
				.collect(Collectors.toMap(StrategyValuationInput::ticker, Function.identity()));

		valuationSnapshotRepository.deleteByTradingDateAndStrategyVersion(tradingDate, strategyVersion);
		valuationSnapshotRepository.flush();
		valuationSnapshotRepository.saveAllAndFlush(results.stream()
				.map(result -> toSnapshot(tradingDate, strategyVersion, result, inputsByTicker.get(result.ticker())))
				.toList());

		return results;
	}

	private ValuationSnapshot toSnapshot(
			LocalDate tradingDate,
			String strategyVersion,
			StrategyEvaluationResult result,
			StrategyValuationInput input
	) {
		return new ValuationSnapshot(
				tradingDate,
				result.ticker(),
				input.closePrice(),
				input.ttmEps(),
				result.currentPer(),
				result.peerAveragePer(),
				result.peerDiscount(),
				strategyVersion
		);
	}
}
