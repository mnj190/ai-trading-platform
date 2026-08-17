package com.mnj190.aitrading.broker;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public record KisOverseasOrderRequest(
		String overseasExchangeCode,
		String productNumber,
		String orderDivision,
		String orderQuantity,
		String overseasOrderUnitPrice,
		String orderServerDivisionCode
) {

	public KisOverseasOrderRequest {
		requireNotBlank(overseasExchangeCode, "overseasExchangeCode");
		requireNotBlank(productNumber, "productNumber");
		requireNotBlank(orderDivision, "orderDivision");
		requireNotBlank(orderQuantity, "orderQuantity");
		requireNotBlank(overseasOrderUnitPrice, "overseasOrderUnitPrice");
		requireNotBlank(orderServerDivisionCode, "orderServerDivisionCode");
	}

	public static KisOverseasOrderRequest usLimitOrder(
			String productNumber,
			BigDecimal quantity,
			BigDecimal price
	) {
		requirePositive(quantity, "quantity");
		requirePositive(price, "price");
		return new KisOverseasOrderRequest(
				"NASD",
				productNumber,
				"00",
				quantity.stripTrailingZeros().toPlainString(),
				price.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
				"0"
		);
	}

	Map<String, String> toBody(KisApiProperties properties) {
		properties.validateAccount();

		Map<String, String> body = new LinkedHashMap<>();
		body.put("CANO", properties.accountNumber());
		body.put("ACNT_PRDT_CD", properties.accountProductCode());
		body.put("OVRS_EXCG_CD", overseasExchangeCode);
		body.put("PDNO", productNumber);
		body.put("ORD_DVSN", orderDivision);
		body.put("ORD_QTY", orderQuantity);
		body.put("OVRS_ORD_UNPR", overseasOrderUnitPrice);
		body.put("ORD_SVR_DVSN_CD", orderServerDivisionCode);
		return body;
	}

	boolean isUsOrder() {
		return overseasExchangeCode.equals("NASD")
				|| overseasExchangeCode.equals("NYSE")
				|| overseasExchangeCode.equals("AMEX");
	}

	private static void requireNotBlank(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
	}

	private static void requirePositive(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
	}
}
