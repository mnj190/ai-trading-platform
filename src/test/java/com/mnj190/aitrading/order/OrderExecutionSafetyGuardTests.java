package com.mnj190.aitrading.order;

import com.mnj190.aitrading.broker.KisApiProperties;
import com.mnj190.aitrading.broker.KisAccessToken;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderExecutionSafetyGuardTests {

	@Test
	void rejectsSubmissionWhenExecutionIsDisabled() {
		OrderExecutionSafetyGuard guard = new OrderExecutionSafetyGuard(
				kisProperties(true),
				new TradingExecutionProperties(false, false)
		);

		assertThatThrownBy(() -> guard.validate(order(), command("1", "100.00")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("disabled");
	}

	@Test
	void rejectsRealTradingUnlessExplicitlyAllowed() {
		OrderExecutionSafetyGuard guard = new OrderExecutionSafetyGuard(
				kisProperties(false),
				new TradingExecutionProperties(true, false)
		);

		assertThatThrownBy(() -> guard.validate(order(), command("1", "100.00")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("real trading");
	}

	@Test
	void allowsPaperOrderWhenExecutionIsEnabled() {
		OrderExecutionSafetyGuard guard = new OrderExecutionSafetyGuard(
				kisProperties(true),
				new TradingExecutionProperties(true, false)
		);

		assertThatCode(() -> guard.validate(order(), command("1", "100.00")))
				.doesNotThrowAnyException();
	}

	@Test
	void allowsRealOrderOnlyWhenExplicitlyAllowed() {
		OrderExecutionSafetyGuard guard = new OrderExecutionSafetyGuard(
				kisProperties(false),
				new TradingExecutionProperties(true, true)
		);

		assertThatCode(() -> guard.validate(order(), command("1", "100.00")))
				.doesNotThrowAnyException();
	}

	private OrderHistory order() {
		return new OrderHistory(
				"GOOGL",
				OrderSide.BUY,
				OrderReason.ENTRY,
				new BigDecimal("100.0000"),
				null,
				OrderType.MARKET,
				OrderStatus.REQUESTED,
				"PE_MEAN_REVERSION_V1",
				OffsetDateTime.now()
		);
	}

	private OrderSubmissionCommand command(String quantity, String limitPrice) {
		return new OrderSubmissionCommand(
				1L,
				new KisAccessToken("Bearer", "token-value", 86400, "2026-08-18 22:00:00"),
				new BigDecimal(quantity),
				new BigDecimal(limitPrice)
		);
	}

	private KisApiProperties kisProperties(boolean paperTrading) {
		return new KisApiProperties(
				paperTrading
						? "https://openapivts.koreainvestment.com:29443"
						: "https://openapi.koreainvestment.com:9443",
				"test-key",
				"test-secret",
				"12345678",
				"01",
				paperTrading
		);
	}
}
