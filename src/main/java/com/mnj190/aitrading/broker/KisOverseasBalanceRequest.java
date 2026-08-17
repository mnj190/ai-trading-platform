package com.mnj190.aitrading.broker;

import java.util.Objects;

public record KisOverseasBalanceRequest(
		String overseasExchangeCode,
		String transactionCurrencyCode,
		String contextAreaFk200,
		String contextAreaNk200,
		String transactionContinuation
) {

	public KisOverseasBalanceRequest {
		requireNotBlank(overseasExchangeCode, "overseasExchangeCode");
		requireNotBlank(transactionCurrencyCode, "transactionCurrencyCode");
		contextAreaFk200 = defaultString(contextAreaFk200);
		contextAreaNk200 = defaultString(contextAreaNk200);
		transactionContinuation = defaultString(transactionContinuation);
	}

	public static KisOverseasBalanceRequest nasdaqUsdFirstPage() {
		return new KisOverseasBalanceRequest("NASD", "USD", "", "", "");
	}

	private static void requireNotBlank(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
	}

	private static String defaultString(String value) {
		return Objects.requireNonNullElse(value, "");
	}
}
