# Daily Evaluation Flow

`PE_MEAN_REVERSION_V1`을 하루 한 번(장 마감 10분 전) 평가하고 매매까지 실행하는 애플리케이션 내부 흐름이다.

## 현재 구현 범위

`KisDailyTradingService.runOnce()`가 아래 단계를 순서대로 실행한다. `kis-server` 프로필에서는
`KisDailyTradingScheduler`가 매일 15:50 America/New_York에 이 메서드를 호출한다.

1. `syncRecentExecutions` — 최근 5일치 KIS 체결 내역을 조회해 `trade_history`/`position_state`에 반영 (최선노력, 실패해도 아래 단계는 계속 진행)
2. KIS 잔고 조회로 매수 가능 현금(USD) 확인
3. 5개 종목의 현재가/PER 조회 → `StrategyValuationInput` 구성
4. 벤치마크(SPY, QQQM) 종가 기록, 계좌 스냅샷 기록 (둘 다 최선노력, 실패해도 아래 단계는 계속 진행)
5. `DailyEvaluationService.evaluateAndCreateOrderRequests`로 ENTRY/SWITCH/EXIT/HOLD 판단 → `valuation_snapshot` 저장 → 필요 시 `order_history`에 `REQUESTED` 주문 생성
6. `REQUESTED` 주문을 다시 조회해 신선한 가격으로 매수 신호 재확인 후 `OrderSubmissionService`로 제출 (주문 하나가 실패해도 나머지는 계속 제출 시도)
7. 제출 직후 한 번 더 `syncRecentExecutions` 실행 — 유동성 있는 종목은 대개 초 단위로 체결되므로, 다음날 스케줄까지 기다리지 않고 당일에 체결을 확인

## 핵심 아님(best-effort) 단계

실행 이력 동기화(1번)와 벤치마크/계좌 스냅샷(4번)은 오늘의 매매 신호 자체에는 필요 없는 부가 기록이다.
`KisDailyTradingService.runBestEffort`로 감싸져 있어서, 여기서 예외가 나도 로그만 남기고 핵심 흐름(신호
계산 → 주문 생성 → 제출)은 중단되지 않는다.

## 안전 원칙

- 자동 주문 실행은 `trading.execution.enabled=true`, `trading.execution.allow-real-trading=true`가 모두
  있어야 동작한다.
- 주문 금액 상한(`max-order-notional-amount`)은 없다 — 전략 규칙(매수 가능한 최대 금액으로 전량 매수)과
  안전장치가 서로 다른 상한을 갖는 게 오히려 혼란을 줄 수 있어서 제거했다. 실제 주문 크기는
  `WholeShareOrderSizer`가 KIS 조회 잔고 안에서만 계산하므로 잔고 이상으로 나갈 수 없다.
- 같은 티커/전략 버전으로 이미 진행 중인 주문(REQUESTED/SUBMITTED/PARTIALLY_FILLED)이 있으면 중복
  주문을 만들지 않는다 (`OrderHistoryRepository.existsByTickerAndStrategyVersionAndStatusIn`).

## 체결 반영

`KisOrderExecutionSyncService`가 KIS 체결 조회 응답을 받아 아래 작업을 수행한다.

1. `brokerOrderId`로 우리 `order_history`와 매칭 (매칭 안 되면 우리 시스템이 낸 주문이 아니므로 스킵 —
   예: 사용자가 모바일 앱으로 직접 낸 주문)
2. `TradeExecutionService.recordFill`을 통해 `trade_history` 저장, `position_state` 생성/갱신,
   `order_history` 상태를 `FILLED`/`PARTIALLY_FILLED`로 갱신
3. 누적 체결 수량 기준으로 idempotent하게 처리하므로, 같은 체결을 여러 번 동기화해도 중복 반영되지 않는다

## 알려진 한계

- `per_normalization_baseline`(5년 평균 PER)은 자동 재계산되지 않는다. 해당 월의 값이 없으면 가장 최근
  값을 이월(carry-forward)해서 쓰고 경고 로그를 남긴다 — 원본 데이터가 외부 조사로 산출되는 값이라
  코드가 스스로 재계산할 방법이 없기 때문이다.
