package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.market.PerNormalizationBaseline;
import com.mnj190.aitrading.market.PerNormalizationBaselineRepository;
import com.mnj190.aitrading.order.OrderHistory;
import com.mnj190.aitrading.strategy.DailyEvaluationCommand;
import com.mnj190.aitrading.strategy.DailyEvaluationReport;
import com.mnj190.aitrading.strategy.DailyEvaluationService;
import com.mnj190.aitrading.strategy.StrategyValuationInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Profile("kis-daily-evaluation")
@Order(1)
class KisDailyEvaluationRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KisDailyEvaluationRunner.class);
	private static final ZoneId US_MARKET_ZONE = ZoneId.of("America/New_York");

	private final KisAccessTokenProvider tokenProvider;
	private final KisOverseasPriceDetailClient priceDetailClient;
	private final KisPriceDetailValuationInputMapper inputMapper;
	private final PerNormalizationBaselineRepository baselineRepository;
	private final DailyEvaluationService dailyEvaluationService;
	private final List<String> symbols;
	private final String strategyVersion;
	private final BigDecimal availableCash;

	KisDailyEvaluationRunner(
			KisAccessTokenProvider tokenProvider,
			KisOverseasPriceDetailClient priceDetailClient,
			KisPriceDetailValuationInputMapper inputMapper,
			PerNormalizationBaselineRepository baselineRepository,
			DailyEvaluationService dailyEvaluationService,
			@Value("${kis.evaluation.symbols:NVDA,GOOGL,AAPL,AMZN,MSFT}") String symbols,
			@Value("${kis.evaluation.strategy-version:PE_MEAN_REVERSION_V1}") String strategyVersion,
			@Value("${kis.evaluation.available-cash:1000.0000}") BigDecimal availableCash
	) {
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.priceDetailClient = Objects.requireNonNull(priceDetailClient);
		this.inputMapper = Objects.requireNonNull(inputMapper);
		this.baselineRepository = Objects.requireNonNull(baselineRepository);
		this.dailyEvaluationService = Objects.requireNonNull(dailyEvaluationService);
		this.symbols = Arrays.stream(symbols.split(","))
				.map(String::trim)
				.filter(symbol -> !symbol.isBlank())
				.toList();
		this.strategyVersion = requireNotBlank(strategyVersion, "strategyVersion");
		this.availableCash = Objects.requireNonNull(availableCash);
	}

	@Override
	public void run(ApplicationArguments args) {
		LocalDate tradingDate = LocalDate.now(US_MARKET_ZONE);
		log.info(
				"Starting KIS daily evaluation: strategyVersion={}, symbols={}, tradingDate={}",
				strategyVersion,
				symbols,
				tradingDate
		);

		KisAccessToken accessToken = tokenProvider.getAccessToken();
		LocalDate baseMonth = YearMonth.from(tradingDate).atDay(1);

		List<StrategyValuationInput> inputs = symbols.stream()
				.map(symbol -> toInput(accessToken, symbol, baseMonth))
				.toList();

		DailyEvaluationReport report = dailyEvaluationService.evaluateAndCreateOrderRequests(new DailyEvaluationCommand(
				tradingDate,
				inputs,
				availableCash,
				strategyVersion,
				OffsetDateTime.now()
		));

		report.evaluation().results().forEach(result -> log.info(
				"KIS daily evaluation result: ticker={}, currentPer={}, fiveYearAveragePer={}, normalizedPer={}, peerAverageNormalizedPer={}, peerDiscount={}",
				result.ticker(),
				result.currentPer(),
				result.fiveYearAveragePer(),
				result.normalizedPer(),
				result.peerAverageNormalizedPer(),
				result.peerDiscount()
		));
		log.info("KIS daily evaluation decision: {}", report.evaluation().decision());
		log.info("KIS daily evaluation requested orders: {}", report.requestedOrders().size());
		for (OrderHistory order : report.requestedOrders()) {
			log.info(
					"Requested order: ticker={}, side={}, reason={}, requestedAmount={}, requestedQuantity={}",
					order.getTicker(),
					order.getSide(),
					order.getOrderReason(),
					order.getRequestedAmount(),
					order.getRequestedQuantity()
			);
		}
		log.info("KIS daily evaluation finished");
	}

	private StrategyValuationInput toInput(KisAccessToken accessToken, String symbol, LocalDate baseMonth) {
		KisOverseasPriceDetailResponse response = priceDetailClient.inquireNasdaqPriceDetail(accessToken, symbol);
		pauseBetweenKisRequests();
		PerNormalizationBaseline baseline = baselineRepository
				.findByTickerAndBaseMonth(symbol, baseMonth)
				.orElseThrow(() -> new IllegalStateException(
						"missing PER normalization baseline for " + symbol + " and baseMonth " + baseMonth
				));
		return inputMapper.toInput(symbol, response, baseline.getFiveYearAveragePer());
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
}
