package com.mnj190.aitrading.broker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record KisOverseasOrderExecutionRequest(
		String productNumber,
		LocalDate orderStartDate,
		LocalDate orderEndDate,
		String sellBuyDivision,
		String executionDivision,
		String overseasExchangeCode,
		String sortSequence,
		String orderDate,
		String orderBranchNumber,
		String orderNumber,
		String contextAreaNk200,
		String contextAreaFk200,
		String transactionContinuation
) {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

	public KisOverseasOrderExecutionRequest {
		Objects.requireNonNull(orderStartDate, "orderStartDate must not be null");
		Objects.requireNonNull(orderEndDate, "orderEndDate must not be null");
		requireNotBlank(sellBuyDivision, "sellBuyDivision");
		requireNotBlank(executionDivision, "executionDivision");
		requireNotBlank(sortSequence, "sortSequence");
		productNumber = defaultString(productNumber);
		overseasExchangeCode = defaultString(overseasExchangeCode);
		orderDate = defaultString(orderDate);
		orderBranchNumber = defaultString(orderBranchNumber);
		orderNumber = defaultString(orderNumber);
		contextAreaNk200 = defaultString(contextAreaNk200);
		contextAreaFk200 = defaultString(contextAreaFk200);
		transactionContinuation = defaultString(transactionContinuation);
	}

	public static KisOverseasOrderExecutionRequest allForDate(LocalDate orderDate) {
		return new KisOverseasOrderExecutionRequest(
				"",
				orderDate,
				orderDate,
				"00",
				"00",
				"",
				"DS",
				"",
				"",
				"",
				"",
				"",
				""
		);
	}

	String formattedOrderStartDate() {
		return orderStartDate.format(DATE_FORMATTER);
	}

	String formattedOrderEndDate() {
		return orderEndDate.format(DATE_FORMATTER);
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
