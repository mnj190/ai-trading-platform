package com.mnj190.aitrading.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PeerAveragePerCalculator {

	private static final int V1_UNIVERSE_SIZE = 5;
	private static final int RESULT_SCALE = 4;

	public BigDecimal calculate(List<PeerPerInput> peerInputs) {
		Objects.requireNonNull(peerInputs, "peerInputs must not be null");
		validateUniverse(peerInputs);

		BigDecimal sum = peerInputs.stream()
				.map(PeerPerInput::currentPer)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return sum.divide(BigDecimal.valueOf(peerInputs.size()), RESULT_SCALE, RoundingMode.HALF_UP);
	}

	private void validateUniverse(List<PeerPerInput> peerInputs) {
		if (peerInputs.size() != V1_UNIVERSE_SIZE) {
			throw new IllegalArgumentException("PE_MEAN_REVERSION_V1 requires exactly 5 peer PER inputs");
		}

		Set<String> tickers = new HashSet<>();
		for (PeerPerInput peerInput : peerInputs) {
			if (!tickers.add(peerInput.ticker())) {
				throw new IllegalArgumentException("duplicate ticker is not allowed: " + peerInput.ticker());
			}
		}
	}
}

