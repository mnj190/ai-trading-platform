package com.mnj190.aitrading.broker;

public record KisOverseasPriceRequest(
		String auth,
		String exchangeCode,
		String symbol,
		String transactionContinuation
) {

	public KisOverseasPriceRequest {
		auth = defaultString(auth);
		requireNotBlank(exchangeCode, "exchangeCode");
		requireNotBlank(symbol, "symbol");
		transactionContinuation = defaultString(transactionContinuation);
	}

	public static KisOverseasPriceRequest nasdaq(String symbol) {
		return new KisOverseasPriceRequest("", "NAS", symbol, "");
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
