package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PeerAverageNormalizedPerCalculator {

	private static final int V1_UNIVERSE_SIZE = 5;
	private static final int RESULT_SCALE = 4;

	public BigDecimal calculate(List<NormalizedPerInput> inputs) {
		Objects.requireNonNull(inputs, "inputs must not be null");
		validateUniverse(inputs);

		BigDecimal sum = inputs.stream()
				.map(NormalizedPerInput::normalizedPer)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return sum.divide(BigDecimal.valueOf(inputs.size()), RESULT_SCALE, RoundingMode.HALF_UP);
	}

	private void validateUniverse(List<NormalizedPerInput> inputs) {
		if (inputs.size() != V1_UNIVERSE_SIZE) {
			throw new IllegalArgumentException("PE_MEAN_REVERSION_V1 requires exactly 5 normalized PER inputs");
		}

		Set<String> tickers = new HashSet<>();
		for (NormalizedPerInput input : inputs) {
			if (!tickers.add(input.ticker())) {
				throw new IllegalArgumentException("duplicate ticker is not allowed: " + input.ticker());
			}
		}
	}
}

