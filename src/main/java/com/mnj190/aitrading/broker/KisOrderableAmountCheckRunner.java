package com.mnj190.aitrading.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Read-only check: compares KIS's orderable-amount ("매수가능금액") response
 * against present-balance's foreign-currency deposit, to verify which field
 * KisDailyTradingService should treat as "available cash". Places no order.
 */
@Component
@Profile("kis-orderable-amount-check")
@Order(1)
class KisOrderableAmountCheckRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KisOrderableAmountCheckRunner.class);

	private final KisAccessTokenProvider tokenProvider;
	private final KisOverseasPriceClient priceClient;
	private final KisOverseasOrderableAmountClient orderableAmountClient;
	private final String symbol;

	KisOrderableAmountCheckRunner(
			KisAccessTokenProvider tokenProvider,
			KisOverseasPriceClient priceClient,
			KisOverseasOrderableAmountClient orderableAmountClient,
			@Value("${kis.check.symbol:AAPL}") String symbol
	) {
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.priceClient = Objects.requireNonNull(priceClient);
		this.orderableAmountClient = Objects.requireNonNull(orderableAmountClient);
		this.symbol = symbol;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Starting KIS orderable amount check: symbol={}", symbol);
		KisAccessToken accessToken = tokenProvider.getAccessToken();

		KisOverseasPriceResponse priceResponse = priceClient.inquireNasdaqPrice(accessToken, symbol);
		log.info("KIS price output: {}", priceResponse.output());
		BigDecimal price = extractPrice(priceResponse);

		KisOverseasOrderableAmountResponse response = orderableAmountClient.inquireOrderableAmount(
				accessToken,
				KisOverseasOrderableAmountRequest.nasdaq(symbol, price)
		);
		log.info(
				"KIS orderable amount response: returnCode={}, messageCode={}, message={}",
				response.returnCode(),
				response.messageCode(),
				response.message()
		);
		log.info("KIS orderable amount output: {}", response.output());
		log.info("KIS orderable amount check finished");
	}

	private BigDecimal extractPrice(KisOverseasPriceResponse response) {
		if (!(response.output() instanceof java.util.Map<?, ?> map)) {
			throw new IllegalStateException("KIS price output is not a map");
		}
		Object last = map.get("last");
		return new BigDecimal(String.valueOf(last));
	}
}
