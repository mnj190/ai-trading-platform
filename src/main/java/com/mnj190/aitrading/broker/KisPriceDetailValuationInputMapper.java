package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.strategy.StrategyValuationInput;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class KisPriceDetailValuationInputMapper {

	private static final int RESULT_SCALE = 4;

	public StrategyValuationInput toInput(
			String ticker,
			KisOverseasPriceDetailResponse response,
			BigDecimal fiveYearAveragePer
	) {
		return toInput(ticker, response, fiveYearAveragePer, extractOutput(response, ticker));
	}

	public BigDecimal currentPer(KisOverseasPriceDetailResponse response, String ticker) {
		Map<?, ?> output = extractOutput(response, ticker);
		return firstDecimal(output, "perx")
				.orElseThrow(() -> new IllegalStateException("KIS price detail does not contain perx for " + ticker));
	}

	private StrategyValuationInput toInput(
			String ticker,
			KisOverseasPriceDetailResponse response,
			BigDecimal fiveYearAveragePer,
			Map<?, ?> output
	) {
		Objects.requireNonNull(response, "response must not be null");
		Objects.requireNonNull(fiveYearAveragePer, "fiveYearAveragePer must not be null");
		BigDecimal closePrice = firstDecimal(output, "last")
				.or(() -> firstDecimal(output, "base"))
				.orElseThrow(() -> new IllegalStateException("KIS price detail does not contain price for " + ticker));
		BigDecimal currentPer = firstDecimal(output, "perx")
				.orElseThrow(() -> new IllegalStateException("KIS price detail does not contain perx for " + ticker));
		BigDecimal ttmEps = firstDecimal(output, "epsx")
				.orElseGet(() -> closePrice.divide(currentPer, RESULT_SCALE, RoundingMode.HALF_UP));

		return new StrategyValuationInput(
				ticker,
				closePrice,
				ttmEps,
				currentPer,
				fiveYearAveragePer
		);
	}

	private Map<?, ?> extractOutput(KisOverseasPriceDetailResponse response, String ticker) {
		Objects.requireNonNull(response, "response must not be null");
		if (!"0".equals(response.returnCode())) {
			throw new IllegalStateException(
					"KIS price detail failed for " + ticker + ": " + response.messageCode() + " " + response.message()
			);
		}
		if (!(response.output() instanceof Map<?, ?> output)) {
			throw new IllegalStateException("KIS price detail output is not a map for " + ticker);
		}
		return output;
	}

	private Optional<BigDecimal> firstDecimal(Map<?, ?> map, String key) {
		Object value = map.get(key);
		if (value == null || value.toString().isBlank()) {
			return Optional.empty();
		}
		return Optional.of(new BigDecimal(value.toString()));
	}
}
