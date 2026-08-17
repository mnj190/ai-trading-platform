package com.mnj190.aitrading.broker;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class KisOrderExecutionFillMapper {

	public List<KisOrderExecutionFill> toFills(
			KisOverseasOrderExecutionResponse response,
			OffsetDateTime fallbackExecutedAt
	) {
		Objects.requireNonNull(response, "response must not be null");
		Objects.requireNonNull(fallbackExecutedAt, "fallbackExecutedAt must not be null");

		if (!"0".equals(response.returnCode())) {
			throw new IllegalStateException(
					"KIS order execution inquiry failed: " + response.messageCode() + " " + response.message()
			);
		}
		if (response.output() == null) {
			return List.of();
		}
		if (!(response.output() instanceof List<?> rows)) {
			throw new IllegalStateException("KIS order execution output is not a list");
		}

		return rows.stream()
				.filter(Map.class::isInstance)
				.map(Map.class::cast)
				.map(row -> toFill(row, fallbackExecutedAt))
				.flatMap(Optional::stream)
				.toList();
	}

	private Optional<KisOrderExecutionFill> toFill(Map<?, ?> row, OffsetDateTime fallbackExecutedAt) {
		Optional<String> brokerOrderId = firstText(row, "odno", "ODNO");
		Optional<String> ticker = firstText(row, "pdno", "PDNO");
		Optional<BigDecimal> executedQuantity = firstDecimal(
				row,
				"ft_ccld_qty",
				"FT_CCLD_QTY",
				"ccld_qty",
				"CCLD_QTY"
		);
		Optional<BigDecimal> executedPrice = firstDecimal(
				row,
				"ft_ccld_unpr3",
				"FT_CCLD_UNPR3",
				"ccld_unpr",
				"CCLD_UNPR"
		);

		if (brokerOrderId.isEmpty()
				|| ticker.isEmpty()
				|| executedQuantity.isEmpty()
				|| executedPrice.isEmpty()
				|| executedQuantity.get().compareTo(BigDecimal.ZERO) <= 0
				|| executedPrice.get().compareTo(BigDecimal.ZERO) <= 0) {
			return Optional.empty();
		}

		return Optional.of(new KisOrderExecutionFill(
				brokerOrderId.get(),
				ticker.get(),
				executedQuantity.get(),
				executedPrice.get(),
				fallbackExecutedAt
		));
	}

	private Optional<String> firstText(Map<?, ?> row, String... keys) {
		for (String key : keys) {
			Object value = row.get(key);
			if (value != null && !value.toString().isBlank()) {
				return Optional.of(value.toString());
			}
		}
		return Optional.empty();
	}

	private Optional<BigDecimal> firstDecimal(Map<?, ?> row, String... keys) {
		return firstText(row, keys).map(BigDecimal::new);
	}
}
