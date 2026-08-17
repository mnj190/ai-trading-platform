package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public record DailyEvaluationCommand(
		LocalDate tradingDate,
		List<StrategyValuationInput> valuationInputs,
		BigDecimal availableCash,
		String strategyVersion,
		OffsetDateTime evaluatedAt
) {

	public DailyEvaluationCommand {
		Objects.requireNonNull(tradingDate, "tradingDate must not be null");
		valuationInputs = List.copyOf(Objects.requireNonNull(valuationInputs, "valuationInputs must not be null"));
		Objects.requireNonNull(availableCash, "availableCash must not be null");
		Objects.requireNonNull(evaluatedAt, "evaluatedAt must not be null");

		if (valuationInputs.isEmpty()) {
			throw new IllegalArgumentException("valuationInputs must not be empty");
		}
		if (availableCash.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("availableCash must be greater than or equal to zero");
		}
		if (strategyVersion == null || strategyVersion.isBlank()) {
			throw new IllegalArgumentException("strategyVersion must not be blank");
		}
	}
}
