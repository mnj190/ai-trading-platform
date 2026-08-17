package com.mnj190.aitrading.broker;

public record KisOverseasPriceDetailRequest(
		String auth,
		String exchangeCode,
		String symbol,
		String transactionContinuation
) {

	public KisOverseasPriceDetailRequest {
		auth = defaultString(auth);
		requireNotBlank(exchangeCode, "exchangeCode");
		requireNotBlank(symbol, "symbol");
		transactionContinuation = defaultString(transactionContinuation);
	}

	public static KisOverseasPriceDetailRequest nasdaq(String symbol) {
		return new KisOverseasPriceDetailRequest("", "NAS", symbol, "");
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
