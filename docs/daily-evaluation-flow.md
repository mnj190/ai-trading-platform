# Daily Evaluation Flow

`PE_MEAN_REVERSION_V1`을 하루 한 번 평가하기 위한 애플리케이션 내부 흐름이다.

## 현재 구현 범위

`DailyEvaluationService`는 아래 작업을 하나의 트랜잭션 흐름으로 묶는다.

1. 활성화된 `strategy_config` 조회
2. `position_state`에서 현재 보유 종목 조회
3. 5개 종목 valuation input 평가
4. `valuation_snapshot` 저장
5. ENTRY/SWITCH/EXIT/HOLD decision 생성
6. decision을 `order_history`의 `REQUESTED` 주문으로 저장

이 단계에서는 KIS 주문 제출을 실행하지 않는다. 실제 주문 제출은 `OrderSubmissionService`와 `OrderExecutionSafetyGuard`를 통과해야 한다.

## 아직 남은 연결

- KIS 가격상세 조회 결과를 `DailyEvaluationCommand`로 만드는 runner/service 정리
- BUY 요청 금액을 실제 주문 수량과 지정가로 변환
- KIS 주문 제출 후 체결 조회
- KIS 체결 조회 응답을 `TradeExecutionService` 입력으로 변환
- 계좌 현황을 `account_snapshot`에 저장
- 같은 거래일 중복 주문 방지

## 안전 원칙

- 자동 주문 실행은 기본 비활성화 상태다.
- 실전 주문은 `trading.execution.enabled=true`, `trading.execution.allow-real-trading=true`가 모두 필요하다.
- 주문 금액은 `trading.execution.max-order-notional-amount`를 초과할 수 없다.

## 체결 반영

`TradeExecutionService`는 `SUBMITTED` 주문의 체결 정보를 받아 아래 작업을 수행한다.

1. `trade_history` 저장
2. BUY 체결 시 `position_state` 생성 또는 동일 종목 보유수량/평균단가 갱신
3. SELL 체결 시 보유수량 감소, 전량 매도면 `position_state` 삭제
4. 주문 상태를 `FILLED`로 변경

아직 KIS 체결 조회 응답을 이 서비스 입력으로 변환하는 sync layer와 중복 체결 방지는 남아있다.
