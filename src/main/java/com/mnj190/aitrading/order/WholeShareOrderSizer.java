package com.mnj190.aitrading.order;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class WholeShareOrderSizer {

	public BigDecimal buyQuantity(BigDecimal availableAmount, BigDecimal limitPrice) {
		if (availableAmount == null || availableAmount.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("availableAmount must be greater than or equal to zero");
		}
		if (limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("limitPrice must be greater than zero");
		}
		return availableAmount.divide(limitPrice, 0, RoundingMode.DOWN);
	}
}
