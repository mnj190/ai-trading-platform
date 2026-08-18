package com.mnj190.aitrading.broker;

import com.mnj190.aitrading.market.BenchmarkSnapshot;
import com.mnj190.aitrading.market.BenchmarkSnapshotRepository;
import com.mnj190.aitrading.market.PerNormalizationBaseline;
import com.mnj190.aitrading.market.PerNormalizationBaselineRepository;
import com.mnj190.aitrading.order.OrderHistory;
import com.mnj190.aitrading.order.OrderSide;
import com.mnj190.aitrading.order.OrderSubmissionCommand;
import com.mnj190.aitrading.order.OrderSubmissionCommandFactory;
import com.mnj190.aitrading.order.OrderSubmissionService;
import com.mnj190.aitrading.portfolio.AccountSnapshot;
import com.mnj190.aitrading.portfolio.AccountSnapshotRepository;
import com.mnj190.aitrading.portfolio.PositionState;
import com.mnj190.aitrading.portfolio.PositionStateRepository;
import com.mnj190.aitrading.strategy.DailyEvaluationCommand;
import com.mnj190.aitrading.strategy.DailyEvaluationReport;
import com.mnj190.aitrading.strategy.DailyEvaluationService;
import com.mnj190.aitrading.strategy.StrategyConfig;
import com.mnj190.aitrading.strategy.StrategyConfigRepository;
import com.mnj190.aitrading.strategy.StrategyEvaluationResult;
import com.mnj190.aitrading.strategy.StrategyValuationInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Evaluates the V1 universe against real KIS data and submits any resulting
 * orders. Shared by the on-demand {@code kis-daily-evaluation} runner and the
 * {@code kis-server} scheduled trigger so both go through the exact same
 * evaluate-then-submit path.
 */
@Component
public class KisDailyTradingService {

	private static final Logger log = LoggerFactory.getLogger(KisDailyTradingService.class);
	private static final ZoneId US_MARKET_ZONE = ZoneId.of("America/New_York");
	private static final int RESULT_SCALE = 4;
	private static final List<String> BENCHMARK_SYMBOLS = List.of("SPY", "QQQM");

	private final KisAccessTokenProvider tokenProvider;
	private final KisOverseasPriceDetailClient priceDetailClient;
	private final KisOverseasPresentBalanceClient presentBalanceClient;
	private final KisOverseasOrderExecutionClient orderExecutionClient;
	private final KisOrderExecutionSyncService orderExecutionSyncService;
	private final KisPriceDetailValuationInputMapper inputMapper;
	private final PerNormalizationBaselineRepository baselineRepository;
	private final StrategyConfigRepository strategyConfigRepository;
	private final DailyEvaluationService dailyEvaluationService;
	private final OrderSubmissionCommandFactory orderSubmissionCommandFactory;
	private final OrderSubmissionService orderSubmissionService;
	private final BenchmarkSnapshotRepository benchmarkSnapshotRepository;
	private final AccountSnapshotRepository accountSnapshotRepository;
	private final PositionStateRepository positionStateRepository;
	private final List<String> symbols;
	private final String strategyVersion;

	public KisDailyTradingService(
			KisAccessTokenProvider tokenProvider,
			KisOverseasPriceDetailClient priceDetailClient,
			KisOverseasPresentBalanceClient presentBalanceClient,
			KisOverseasOrderExecutionClient orderExecutionClient,
			KisOrderExecutionSyncService orderExecutionSyncService,
			KisPriceDetailValuationInputMapper inputMapper,
			PerNormalizationBaselineRepository baselineRepository,
			StrategyConfigRepository strategyConfigRepository,
			DailyEvaluationService dailyEvaluationService,
			OrderSubmissionCommandFactory orderSubmissionCommandFactory,
			OrderSubmissionService orderSubmissionService,
			BenchmarkSnapshotRepository benchmarkSnapshotRepository,
			AccountSnapshotRepository accountSnapshotRepository,
			PositionStateRepository positionStateRepository,
			@Value("${kis.evaluation.symbols:NVDA,GOOGL,AAPL,AMZN,MSFT}") String symbols,
			@Value("${kis.evaluation.strategy-version:PE_MEAN_REVERSION_V1}") String strategyVersion
	) {
		this.tokenProvider = Objects.requireNonNull(tokenProvider);
		this.priceDetailClient = Objects.requireNonNull(priceDetailClient);
		this.presentBalanceClient = Objects.requireNonNull(presentBalanceClient);
		this.orderExecutionClient = Objects.requireNonNull(orderExecutionClient);
		this.orderExecutionSyncService = Objects.requireNonNull(orderExecutionSyncService);
		this.inputMapper = Objects.requireNonNull(inputMapper);
		this.baselineRepository = Objects.requireNonNull(baselineRepository);
		this.strategyConfigRepository = Objects.requireNonNull(strategyConfigRepository);
		this.dailyEvaluationService = Objects.requireNonNull(dailyEvaluationService);
		this.orderSubmissionCommandFactory = Objects.requireNonNull(orderSubmissionCommandFactory);
		this.orderSubmissionService = Objects.requireNonNull(orderSubmissionService);
		this.benchmarkSnapshotRepository = Objects.requireNonNull(benchmarkSnapshotRepository);
		this.accountSnapshotRepository = Objects.requireNonNull(accountSnapshotRepository);
		this.positionStateRepository = Objects.requireNonNull(positionStateRepository);
		this.symbols = Arrays.stream(symbols.split(","))
				.map(String::trim)
				.filter(symbol -> !symbol.isBlank())
				.toList();
		this.strategyVersion = requireNotBlank(strategyVersion, "strategyVersion");
	}

	public void runOnce() {
		LocalDate tradingDate = LocalDate.now(US_MARKET_ZONE);
		log.info(
				"Starting KIS daily evaluation: strategyVersion={}, symbols={}, tradingDate={}",
				strategyVersion,
				symbols,
				tradingDate
		);

		KisAccessToken accessToken = tokenProvider.getAccessToken();

		syncRecentExecutions(accessToken);
		pauseBetweenKisRequests();

		BigDecimal availableCash = resolveAvailableCashUsd(accessToken);
		log.info("Resolved available cash from KIS present balance: {}", availableCash);
		pauseBetweenKisRequests();

		LocalDate baseMonth = YearMonth.from(tradingDate).atDay(1);
		List<StrategyValuationInput> inputs = symbols.stream()
				.map(symbol -> toInput(accessToken, symbol, baseMonth))
				.toList();
		Map<String, StrategyValuationInput> inputsByTicker = inputs.stream()
				.collect(Collectors.toMap(StrategyValuationInput::ticker, Function.identity()));

		recordBenchmarkSnapshots(accessToken, tradingDate);
		recordAccountSnapshot(tradingDate, availableCash, inputsByTicker);

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

		BigDecimal entryThreshold = strategyConfigRepository.findByStrategyVersionAndEnabledTrue(strategyVersion)
				.map(StrategyConfig::getEntryThreshold)
				.orElseThrow(() -> new IllegalStateException("enabled strategy config not found: " + strategyVersion));
		BigDecimal peerAverageNormalizedPer = report.evaluation().results().stream()
				.findFirst()
				.map(StrategyEvaluationResult::peerAverageNormalizedPer)
				.orElse(null);

		for (OrderHistory order : report.requestedOrders()) {
			submitOrder(accessToken, order, inputsByTicker, peerAverageNormalizedPer, entryThreshold);
			pauseBetweenKisRequests();
		}

		log.info("KIS daily evaluation finished");
	}

	private void submitOrder(
			KisAccessToken accessToken,
			OrderHistory order,
			Map<String, StrategyValuationInput> inputsByTicker,
			BigDecimal peerAverageNormalizedPer,
			BigDecimal entryThreshold
	) {
		KisOverseasPriceDetailResponse freshResponse =
				priceDetailClient.inquireNasdaqPriceDetail(accessToken, order.getTicker());
		BigDecimal freshPrice = inputMapper.closePrice(freshResponse, order.getTicker());

		if (order.getSide() == OrderSide.BUY) {
			StrategyValuationInput baselineInput = inputsByTicker.get(order.getTicker());
			BigDecimal freshCurrentPer = inputMapper.currentPer(freshResponse, order.getTicker());
			BigDecimal freshNormalizedPer = freshCurrentPer.divide(
					baselineInput.fiveYearAveragePer(),
					RESULT_SCALE,
					RoundingMode.HALF_UP
			);
			BigDecimal freshPeerDiscount = freshNormalizedPer
					.divide(peerAverageNormalizedPer, RESULT_SCALE, RoundingMode.HALF_UP)
					.subtract(BigDecimal.ONE);
			if (freshPeerDiscount.compareTo(entryThreshold) > 0) {
				log.warn(
						"Skipping submission for {}: buy signal no longer valid on re-check, freshPeerDiscount={} > entryThreshold={}",
						order.getTicker(),
						freshPeerDiscount,
						entryThreshold
				);
				return;
			}
			log.info("Buy signal re-confirmed for {}: freshPeerDiscount={}", order.getTicker(), freshPeerDiscount);
		}

		OrderSubmissionCommand command = orderSubmissionCommandFactory.create(order, accessToken, freshPrice);
		OrderHistory submitted = orderSubmissionService.submit(command);
		log.info(
				"Submitted order: ticker={}, side={}, status={}, brokerOrderId={}, limitPrice={}",
				submitted.getTicker(),
				submitted.getSide(),
				submitted.getStatus(),
				submitted.getBrokerOrderId(),
				freshPrice
		);
	}

	private void syncRecentExecutions(KisAccessToken accessToken) {
		LocalDate today = LocalDate.now();
		LocalDate windowStart = today.minusDays(5);
		KisOverseasOrderExecutionResponse response = orderExecutionClient.inquireExecutions(
				accessToken,
				new KisOverseasOrderExecutionRequest(
						"",
						windowStart,
						today,
						"00",
						"00",
						"",
						"DS",
						"",
						"",
						"",
						"",
						"",
						""
				)
		);
		ExecutionSyncReport report = orderExecutionSyncService.sync(response, OffsetDateTime.now());
		log.info(
				"Synced KIS executions: receivedFills={}, recordedFills={}, skippedUnknownOrders={}, skippedNonSubmittedOrders={}, skippedAlreadyUpToDate={}",
				report.receivedFills(),
				report.recordedFills(),
				report.skippedUnknownOrders(),
				report.skippedNonSubmittedOrders(),
				report.skippedAlreadyUpToDate()
		);
	}

	private void recordBenchmarkSnapshots(KisAccessToken accessToken, LocalDate tradingDate) {
		for (String symbol : BENCHMARK_SYMBOLS) {
			if (benchmarkSnapshotRepository.findByBenchmarkSymbolAndSnapshotDate(symbol, tradingDate).isPresent()) {
				continue;
			}
			KisOverseasPriceDetailResponse response = priceDetailClient.inquireNasdaqPriceDetail(accessToken, symbol);
			BigDecimal closePrice = inputMapper.closePrice(response, symbol);
			benchmarkSnapshotRepository.save(new BenchmarkSnapshot(symbol, tradingDate, closePrice));
			log.info("Recorded benchmark snapshot: symbol={}, tradingDate={}, closePrice={}", symbol, tradingDate, closePrice);
			pauseBetweenKisRequests();
		}
	}

	private void recordAccountSnapshot(
			LocalDate tradingDate,
			BigDecimal availableCash,
			Map<String, StrategyValuationInput> inputsByTicker
	) {
		if (accountSnapshotRepository.findBySnapshotDate(tradingDate).isPresent()) {
			return;
		}

		BigDecimal stockMarketValue = BigDecimal.ZERO;
		BigDecimal unrealizedPnl = BigDecimal.ZERO;
		Optional<PositionState> holding = positionStateRepository.findByStrategyVersion(strategyVersion);
		if (holding.isPresent()) {
			PositionState position = holding.get();
			StrategyValuationInput input = inputsByTicker.get(position.getTicker());
			if (input != null) {
				stockMarketValue = position.getQuantity().multiply(input.closePrice());
				unrealizedPnl = stockMarketValue.subtract(position.getInvestedAmount());
			}
		}
		BigDecimal totalEquity = availableCash.add(stockMarketValue);

		accountSnapshotRepository.save(new AccountSnapshot(
				tradingDate,
				totalEquity,
				availableCash,
				stockMarketValue,
				unrealizedPnl,
				"USD"
		));
		log.info(
				"Recorded account snapshot: tradingDate={}, totalEquity={}, cashBalance={}, stockMarketValue={}, unrealizedPnl={}",
				tradingDate,
				totalEquity,
				availableCash,
				stockMarketValue,
				unrealizedPnl
		);
	}

	private BigDecimal resolveAvailableCashUsd(KisAccessToken accessToken) {
		KisOverseasPresentBalanceResponse response = presentBalanceClient.inquirePresentBalance(
				accessToken,
				KisOverseasPresentBalanceRequest.allForeignCurrency()
		);
		if (!"0".equals(response.returnCode())) {
			throw new IllegalStateException(
					"KIS present balance failed: " + response.messageCode() + " " + response.message()
			);
		}
		if (!(response.output2() instanceof List<?> rows)) {
			throw new IllegalStateException("KIS present balance output2 is not a list");
		}
		return rows.stream()
				.filter(Map.class::isInstance)
				.map(row -> (Map<?, ?>) row)
				.filter(row -> "USD".equals(String.valueOf(row.get("crcy_cd"))))
				.map(row -> row.get("frcr_dncl_amt_2"))
				.filter(Objects::nonNull)
				.map(value -> new BigDecimal(value.toString()))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"KIS present balance did not contain a USD frcr_dncl_amt_2 entry"
				));
	}

	private StrategyValuationInput toInput(KisAccessToken accessToken, String symbol, LocalDate baseMonth) {
		KisOverseasPriceDetailResponse response = priceDetailClient.inquireNasdaqPriceDetail(accessToken, symbol);
		pauseBetweenKisRequests();
		PerNormalizationBaseline baseline = resolveBaseline(symbol, baseMonth);
		return inputMapper.toInput(symbol, response, baseline.getFiveYearAveragePer());
	}

	private PerNormalizationBaseline resolveBaseline(String symbol, LocalDate baseMonth) {
		Optional<PerNormalizationBaseline> exact = baselineRepository.findByTickerAndBaseMonth(symbol, baseMonth);
		if (exact.isPresent()) {
			return exact.get();
		}
		PerNormalizationBaseline carriedForward = baselineRepository
				.findFirstByTickerAndBaseMonthLessThanEqualOrderByBaseMonthDesc(symbol, baseMonth)
				.orElseThrow(() -> new IllegalStateException(
						"no PER normalization baseline found for " + symbol + " at or before baseMonth " + baseMonth
				));
		log.warn(
				"No PER normalization baseline for {} at baseMonth={}; carrying forward baseMonth={} (fiveYearAveragePer={}) instead. Recalculate and load a fresh baseline for this month when possible.",
				symbol,
				baseMonth,
				carriedForward.getBaseMonth(),
				carriedForward.getFiveYearAveragePer()
		);
		return carriedForward;
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
