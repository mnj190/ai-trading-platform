package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.order.OrderSide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
@Profile("kis-paper-order-smoke")
@Order(1)
class KisPaperOrderSmokeRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KisPaperOrderSmokeRunner.class);
	private static final BigDecimal DEFAULT_LIMIT_BUFFER_RATE = new BigDecimal("0.01");

	private final KisApiProperties properties;
	private final KisAccessTokenProvider tokenProvider;
	private final KisOverseasPriceClient priceClient;
	private final KisOverseasOrderableAmountClient orderableAmountClient;
	private final KisOverseasOrderClient orderClient;
	private final KisOverseasOrderExecutionClient executionClient;
	private final Clock clock;
	private final String symbol;
	private final BigDecimal quantity;
	private final BigDecimal maxNotionalAmount;
	private final BigDecimal configuredLimitPrice;

	KisPaperOrderSmokeRunner(
			KisApiProperties properties,
			KisAccessTokenProvider tokenProvider,
			KisOverseasPriceClient priceClient,
			KisOverseasOrderableAmountClient orderableAmountClient,
			KisOverseasOrderClient orderClient,
			KisOverseasOrderExecutionClient executionClient,
			Clock clock,
			@Value("${kis.smoke.order.symbol:AAPL}") String symbol,
			@Value("${kis.smoke.order.quantity:1}") BigDecimal quantity,
			@Value("${kis.smoke.order.max-notional-amount:500}") BigDecimal maxNotionalAmount,
			@Value("${kis.smoke.order.limit-price:0}") BigDecimal configuredLimitPrice
	) {
		this.properties = Objects.requireNonNull(properties);
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.priceClient = Objects.requireNonNull(priceClient);
		this.orderableAmountClient = Objects.requireNonNull(orderableAmountClient);
		this.orderClient = Objects.requireNonNull(orderClient);
		this.executionClient = Objects.requireNonNull(executionClient);
		this.clock = Objects.requireNonNull(clock);
		this.symbol = requireNotBlank(symbol, "symbol");
		this.quantity = requirePositive(quantity, "quantity");
		this.maxNotionalAmount = requirePositive(maxNotionalAmount, "maxNotionalAmount");
		this.configuredLimitPrice = Objects.requireNonNull(configuredLimitPrice);
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!properties.paperTrading()) {
			throw new IllegalStateException("kis-paper-order-smoke can run only when kis.api.paper-trading=true");
		}

		log.info("Starting KIS paper order smoke test: symbol={}, quantity={}", symbol, quantity);
		KisAccessToken accessToken = tokenProvider.getAccessToken();

		KisOverseasPriceResponse priceResponse = priceClient.inquireNasdaqPrice(accessToken, symbol);
		log.info(
				"KIS paper price response: returnCode={}, messageCode={}, message={}",
				priceResponse.returnCode(),
				priceResponse.messageCode(),
				priceResponse.message()
		);
		log.info("KIS paper price output: {}", priceResponse.output());

		BigDecimal limitPrice = resolveLimitPrice(priceResponse)
				.setScale(2, RoundingMode.HALF_UP);
		BigDecimal notionalAmount = limitPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
		if (notionalAmount.compareTo(maxNotionalAmount) > 0) {
			throw new IllegalStateException(
					"paper order notional amount exceeds smoke-test cap: " + notionalAmount
							+ " > " + maxNotionalAmount
			);
		}

		pauseBetweenKisRequests();
		KisOverseasOrderableAmountResponse orderableAmountResponse =
				orderableAmountClient.inquireOrderableAmount(
						accessToken,
						KisOverseasOrderableAmountRequest.nasdaq(symbol, limitPrice)
				);
		log.info(
				"KIS paper orderable amount response: returnCode={}, messageCode={}, message={}",
				orderableAmountResponse.returnCode(),
				orderableAmountResponse.messageCode(),
				orderableAmountResponse.message()
		);
		log.info("KIS paper orderable amount output: {}", orderableAmountResponse.output());

		pauseBetweenKisRequests();
		try {
			KisOverseasOrderResponse orderResponse = orderClient.placeOrder(
					accessToken,
					OrderSide.BUY,
					KisOverseasOrderRequest.usLimitOrder(symbol, quantity, limitPrice)
			);
			log.info(
					"KIS paper order response: returnCode={}, messageCode={}, message={}, brokerOrderId={}",
					orderResponse.returnCode(),
					orderResponse.messageCode(),
					orderResponse.message(),
					orderResponse.brokerOrderId().orElse("")
			);
			log.info("KIS paper order output: {}", orderResponse.output());
		}
		catch (RestClientResponseException ex) {
			log.warn(
					"KIS paper order request failed: status={}, body={}",
					ex.getStatusCode(),
					ex.getResponseBodyAsString()
			);
		}

		pauseBetweenKisRequests();
		LocalDate orderDate = LocalDate.now(clock);
		KisOverseasOrderExecutionResponse executionResponse = executionClient.inquireExecutions(
				accessToken,
				KisOverseasOrderExecutionRequest.allForDate(orderDate)
		);
		log.info(
				"KIS paper execution response: returnCode={}, messageCode={}, message={}",
				executionResponse.returnCode(),
				executionResponse.messageCode(),
				executionResponse.message()
		);
		log.info("KIS paper execution output: {}", executionResponse.output());
		log.info("KIS paper order smoke test finished");
	}

	private BigDecimal resolveLimitPrice(KisOverseasPriceResponse response) {
		if (configuredLimitPrice.compareTo(BigDecimal.ZERO) > 0) {
			return configuredLimitPrice;
		}
		return extractCurrentPrice(response.output())
				.map(price -> price.multiply(BigDecimal.ONE.add(DEFAULT_LIMIT_BUFFER_RATE)))
				.orElseThrow(() -> new IllegalStateException(
						"could not resolve current price from KIS price output; set kis.smoke.order.limit-price"
				));
	}

	private Optional<BigDecimal> extractCurrentPrice(Object output) {
		if (!(output instanceof Map<?, ?> outputMap)) {
			return Optional.empty();
		}
		return firstDecimal(outputMap, "last")
				.or(() -> firstDecimal(outputMap, "ovrs_nmix_prpr"))
				.or(() -> firstDecimal(outputMap, "base"));
	}

	private Optional<BigDecimal> firstDecimal(Map<?, ?> map, String key) {
		Object value = map.get(key);
		if (value == null || value.toString().isBlank()) {
			return Optional.empty();
		}
		return Optional.of(new BigDecimal(value.toString()));
	}

	private void pauseBetweenKisRequests() {
		try {
			Thread.sleep(1_200);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("interrupted while waiting for KIS request throttle", ex);
		}
	}

	private static String requireNotBlank(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return value;
	}

	private static BigDecimal requirePositive(BigDecimal value, String name) {
		if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException(name + " must be greater than zero");
		}
		return value;
	}
}
