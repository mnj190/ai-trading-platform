package com.mnj190.aitrading.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Profile("kis-universe-price-detail-smoke")
@Order(1)
class KisUniversePriceDetailSmokeRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KisUniversePriceDetailSmokeRunner.class);

	private final KisAccessTokenProvider tokenProvider;
	private final KisOverseasPriceDetailClient priceDetailClient;
	private final List<String> symbols;

	KisUniversePriceDetailSmokeRunner(
			KisAccessTokenProvider tokenProvider,
			KisOverseasPriceDetailClient priceDetailClient,
			@Value("${kis.smoke.strategy.symbols:NVDA,GOOGL,AAPL,AMZN,MSFT}") String symbols
	) {
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.priceDetailClient = Objects.requireNonNull(priceDetailClient);
		this.symbols = Arrays.stream(symbols.split(","))
				.map(String::trim)
				.filter(symbol -> !symbol.isBlank())
				.toList();
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Starting KIS universe price detail smoke test: symbols={}", symbols);
		KisAccessToken accessToken = tokenProvider.getAccessToken();

		for (String symbol : symbols) {
			KisOverseasPriceDetailResponse response = priceDetailClient.inquireNasdaqPriceDetail(accessToken, symbol);
			log.info(
					"KIS price detail {} response: returnCode={}, messageCode={}, message={}",
					symbol,
					response.returnCode(),
					response.messageCode(),
					response.message()
			);
			log.info("KIS price detail {} output: {}", symbol, response.output());
			pauseBetweenKisRequests();
		}
		log.info("KIS universe price detail smoke test finished");
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
}
