package com.mnj190.aitrading.broker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("kis-present-balance-smoke")
@Order(1)
class KisPresentBalanceSmokeRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KisPresentBalanceSmokeRunner.class);

	private final KisAccessTokenProvider tokenProvider;
	private final KisOverseasPresentBalanceClient presentBalanceClient;

	KisPresentBalanceSmokeRunner(
			KisAccessTokenProvider tokenProvider,
			KisOverseasPresentBalanceClient presentBalanceClient
	) {
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.presentBalanceClient = Objects.requireNonNull(presentBalanceClient);
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Starting KIS present balance smoke test");
		KisAccessToken accessToken = tokenProvider.getAccessToken();

		logPresentBalance("KRW", presentBalanceClient.inquirePresentBalance(
				accessToken,
				KisOverseasPresentBalanceRequest.allWon()
		));
		logPresentBalance("Foreign Currency", presentBalanceClient.inquirePresentBalance(
				accessToken,
				KisOverseasPresentBalanceRequest.allForeignCurrency()
		));
		log.info("KIS present balance smoke test finished");
	}

	private void logPresentBalance(String label, KisOverseasPresentBalanceResponse response) {
		log.info(
				"KIS present balance {} response: returnCode={}, messageCode={}, message={}",
				label,
				response.returnCode(),
				response.messageCode(),
				response.message()
		);
		log.info("KIS present balance {} output1: {}", label, response.output1());
		log.info("KIS present balance {} output2: {}", label, response.output2());
		log.info("KIS present balance {} output3: {}", label, response.output3());
	}
}
