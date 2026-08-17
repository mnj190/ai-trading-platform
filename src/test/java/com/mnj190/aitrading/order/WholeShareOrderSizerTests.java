package com.mnj190.aitrading.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WholeShareOrderSizerTests {

	private final WholeShareOrderSizer sizer = new WholeShareOrderSizer();

	@Test
	void calculatesWholeShareBuyQuantity() {
		BigDecimal quantity = sizer.buyQuantity(new BigDecimal("1000.00"), new BigDecimal("225.13"));

		assertThat(quantity).isEqualByComparingTo("4");
	}

	@Test
	void returnsZeroWhenAmountIsLessThanOneShare() {
		BigDecimal quantity = sizer.buyQuantity(new BigDecimal("0.70"), new BigDecimal("225.13"));

		assertThat(quantity).isEqualByComparingTo("0");
	}

	@Test
	void rejectsNonPositiveLimitPrice() {
		assertThatThrownBy(() -> sizer.buyQuantity(new BigDecimal("1000.00"), BigDecimal.ZERO))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("limitPrice");
	}
}
