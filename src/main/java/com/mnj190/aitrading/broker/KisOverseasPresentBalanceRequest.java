package com.mnj190.aitrading.broker;

public record KisOverseasPresentBalanceRequest(
		String wonForeignCurrencyDivisionCode,
		String nationCode,
		String tradingMarketCode,
		String inquiryDivisionCode,
		String transactionContinuation
) {

	public KisOverseasPresentBalanceRequest {
		requireNotBlank(wonForeignCurrencyDivisionCode, "wonForeignCurrencyDivisionCode");
		requireNotBlank(nationCode, "nationCode");
		requireNotBlank(tradingMarketCode, "tradingMarketCode");
		requireNotBlank(inquiryDivisionCode, "inquiryDivisionCode");
		transactionContinuation = defaultString(transactionContinuation);
	}

	public static KisOverseasPresentBalanceRequest allWon() {
		return new KisOverseasPresentBalanceRequest("01", "000", "00", "00", "");
	}

	public static KisOverseasPresentBalanceRequest allForeignCurrency() {
		return new KisOverseasPresentBalanceRequest("02", "000", "00", "00", "");
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
