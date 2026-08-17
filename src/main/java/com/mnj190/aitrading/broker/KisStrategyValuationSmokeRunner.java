package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.market.PerNormalizationBaseline;
import com.mnj190.aitrading.market.PerNormalizationBaselineRepository;
import com.mnj190.aitrading.strategy.StrategyEvaluationRecorder;
import com.mnj190.aitrading.strategy.StrategyEvaluationResult;
import com.mnj190.aitrading.strategy.StrategyRuleConfig;
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
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@Profile("kis-strategy-valuation-smoke")
@Order(1)
class KisStrategyValuationSmokeRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(KisStrategyValuationSmokeRunner.class);

	private final KisAccessTokenProvider tokenProvider;
	private final KisOverseasPriceDetailClient priceDetailClient;
	private final KisPriceDetailValuationInputMapper inputMapper;
	private final PerNormalizationBaselineRepository baselineRepository;
	private final StrategyEvaluationRecorder evaluationRecorder;
	private final Clock clock;
	private final List<String> symbols;
	private final LocalDate baseMonth;
	private final String strategyVersion;
	private final boolean allowCurrentPerBaseline;

	KisStrategyValuationSmokeRunner(
			KisAccessTokenProvider tokenProvider,
			KisOverseasPriceDetailClient priceDetailClient,
			KisPriceDetailValuationInputMapper inputMapper,
			PerNormalizationBaselineRepository baselineRepository,
			StrategyEvaluationRecorder evaluationRecorder,
			Clock clock,
			@Value("${kis.smoke.strategy.symbols:NVDA,GOOGL,AAPL,AMZN,MSFT}") String symbols,
			@Value("${kis.smoke.strategy.base-month:}") String baseMonth,
			@Value("${kis.smoke.strategy.strategy-version:PE_MEAN_REVERSION_V1_SMOKE}") String strategyVersion,
			@Value("${kis.smoke.strategy.allow-current-per-baseline:false}") boolean allowCurrentPerBaseline
	) {
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.priceDetailClient = Objects.requireNonNull(priceDetailClient);
		this.inputMapper = Objects.requireNonNull(inputMapper);
		this.baselineRepository = Objects.requireNonNull(baselineRepository);
		this.evaluationRecorder = Objects.requireNonNull(evaluationRecorder);
		this.clock = Objects.requireNonNull(clock);
		this.symbols = Arrays.stream(symbols.split(","))
				.map(String::trim)
				.filter(symbol -> !symbol.isBlank())
				.toList();
		this.baseMonth = parseBaseMonth(baseMonth, clock);
		this.strategyVersion = requireNotBlank(strategyVersion, "strategyVersion");
		this.allowCurrentPerBaseline = allowCurrentPerBaseline;
	}

	@Override
	public void run(ApplicationArguments args) {
		log.info(
				"Starting KIS strategy valuation smoke test: strategyVersion={}, symbols={}, baseMonth={}",
				strategyVersion,
				symbols,
				baseMonth
		);
		KisAccessToken accessToken = tokenProvider.getAccessToken();

		List<StrategyValuationInput> inputs = symbols.stream()
				.map(symbol -> toInput(accessToken, symbol))
				.toList();

		List<StrategyEvaluationResult> results = evaluationRecorder.evaluateAndRecord(
				LocalDate.now(clock),
				inputs,
				Optional.empty(),
				StrategyRuleConfig.peMeanReversionV1(),
				strategyVersion
		);

		results.forEach(result -> log.info(
				"Strategy valuation result: ticker={}, currentPer={}, fiveYearAveragePer={}, normalizedPer={}, peerAverageNormalizedPer={}, peerDiscount={}",
				result.ticker(),
				result.currentPer(),
				result.fiveYearAveragePer(),
				result.normalizedPer(),
				result.peerAverageNormalizedPer(),
				result.peerDiscount()
		));
		log.info("KIS strategy valuation smoke test finished");
	}

	private StrategyValuationInput toInput(KisAccessToken accessToken, String symbol) {
		KisOverseasPriceDetailResponse response = priceDetailClient.inquireNasdaqPriceDetail(accessToken, symbol);
		pauseBetweenKisRequests();
		PerNormalizationBaseline baseline = baselineRepository
				.findByTickerAndBaseMonth(symbol, baseMonth)
				.orElse(null);
		if (baseline != null) {
			return inputMapper.toInput(symbol, response, baseline.getFiveYearAveragePer());
		}
		if (allowCurrentPerBaseline) {
			BigDecimal currentPer = inputMapper.currentPer(response, symbol);
			log.warn(
					"Using current PER as temporary smoke baseline: ticker={}, baseMonth={}, currentPer={}",
					symbol,
					baseMonth,
					currentPer
			);
			return inputMapper.toInput(symbol, response, currentPer);
		}
		throw new IllegalStateException(
				"missing PER normalization baseline for " + symbol + " and baseMonth " + baseMonth
		);
	}

	private static LocalDate parseBaseMonth(String value, Clock clock) {
		if (value == null || value.isBlank()) {
			return YearMonth.now(clock).atDay(1);
		}
		return YearMonth.parse(value).atDay(1);
	}

	private static String requireNotBlank(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return value;
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
