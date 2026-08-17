package com.mnj190.aitrading.broker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("kis-smoke")
class KisBalanceSmokeRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KisBalanceSmokeRunner.class);

	private final KisTokenClient tokenClient;
	private final KisOverseasBalanceClient balanceClient;

	KisBalanceSmokeRunner(
			KisTokenClient tokenClient,
			KisOverseasBalanceClient balanceClient
	) {
		this.tokenClient = Objects.requireNonNull(tokenClient);
		this.balanceClient = Objects.requireNonNull(balanceClient);
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info("Starting KIS balance smoke test");
		KisAccessToken accessToken = tokenClient.issueAccessToken();
		KisOverseasBalanceResponse response = balanceClient.inquireNasdaqUsdBalance(accessToken);

		log.info(
				"KIS balance response: returnCode={}, messageCode={}, message={}",
				response.returnCode(),
				response.messageCode(),
				response.message()
		);
		log.info("KIS balance holdings: {}", toText(response.output1()));
		log.info("KIS balance summary: {}", toText(response.output2()));
		log.info("KIS balance smoke test finished");
	}

	private String toText(Object value) {
		return String.valueOf(value);
	}
}
