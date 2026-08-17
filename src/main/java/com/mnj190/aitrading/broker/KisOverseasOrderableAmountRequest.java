package com.mnj190.aitrading.broker;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record KisOverseasOrderableAmountRequest(
		String overseasExchangeCode,
		String overseasOrderUnitPrice,
		String itemCode,
		String transactionContinuation
) {

	public KisOverseasOrderableAmountRequest {
		requireNotBlank(overseasExchangeCode, "overseasExchangeCode");
		requireNotBlank(overseasOrderUnitPrice, "overseasOrderUnitPrice");
		requireNotBlank(itemCode, "itemCode");
		transactionContinuation = defaultString(transactionContinuation);
	}

	public static KisOverseasOrderableAmountRequest nasdaq(String itemCode, BigDecimal orderUnitPrice) {
		if (orderUnitPrice == null || orderUnitPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("orderUnitPrice must be greater than zero");
		}
		return new KisOverseasOrderableAmountRequest(
				"NASD",
				orderUnitPrice.setScale(2, RoundingMode.HALF_UP).toPlainString(),
				itemCode,
				""
		);
	}

	private static void requireNotBlank(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
	}

	private static String defaultString(String value) {
		if (value == null) {
			return "";
		}
		return value;
	}
}
