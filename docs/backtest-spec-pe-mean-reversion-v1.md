# PE_MEAN_REVERSION_V1 백테스트 스펙

작성 목적: 현재 코드베이스(main 브랜치, 워킹트리 반영)에 구현된 `PE_MEAN_REVERSION_V1` 전략 로직을 그대로 옮겨 적어, 외부(제미나이)에서 동일한 규칙으로 백테스트를 수행할 수 있도록 정리한 문서.

## 1. 전략 개요

- **전략명**: `PE_MEAN_REVERSION_V1`
- **유형**: 정규화 PER 기반 상대가치 로테이션 전략
- **핵심 아이디어**: 고정된 5종목 유니버스 안에서, "자기 과거 5년 평균 PER 대비 얼마나 싼가"를 정규화한 뒤, 그 정규화 값이 그룹 평균보다 가장 싼 종목 1개만 보유한다. 더 싼 후보가 나타나면 교체(SWITCH)하고, 보유 종목이 더 이상 저평가가 아니면 청산(EXIT)한다.
- 기존 3단계 분할매수(BUY1/BUY2/BUY3) 모델은 완전히 폐기되었고, 단일 포지션 로테이션 모델로 교체됨.

## 2. 유니버스

- 정확히 **5개 종목** 고정
  - NVDA
  - GOOGL
  - AAPL
  - AMZN
  - MSFT
- 코드상 `V1_UNIVERSE_SIZE = 5`로 입력 개수를 검증한다.
- 유니버스 내 중복 티커 불가

## 3. 필요 입력 데이터 (일별 × 종목별)

| 필드 | 설명 |
|---|---|
| ticker | 종목 코드 |
| close_price | 당일 종가 |
| ttm_eps | 최근 12개월 EPS |
| current_per | 당일 현재 PER (= close_price / ttm_eps) |
| five_year_average_per | 해당 종목의 자체 5년 평균 PER (정규화 기준값) |

## 4. 계산 공식 (스케일 4자리, 반올림 HALF_UP)

```
normalizedPer          = currentPer / fiveYearAveragePer
peerAverageNormalizedPer = mean(normalizedPer of all 5 tickers in universe)
peerDiscount            = normalizedPer / peerAverageNormalizedPer - 1
```

- `peerDiscount < 0` → 그룹 평균보다 상대적으로 저평가
- `peerDiscount > 0` → 그룹 평균보다 상대적으로 고평가

## 5. 전략 파라미터 (기본값)

| 파라미터 | 값 | 의미 |
|---|---|---|
| entryThreshold | -0.1500 | 무포지션 상태에서 신규 진입 조건 (peerDiscount ≤ -15%) |
| switchThreshold | 0.0500 | 보유 종목보다 최소 5%p 더 저평가된 후보가 있어야 교체 |
| exitThreshold | 0.0000 | 보유 종목 discount ≥ 0(그룹 평균 이상)이면 청산 |
| maxPositions | 1 | 동시에 1개 종목만 보유 |

제약: `entryThreshold < exitThreshold`, `switchThreshold > 0`.

## 6. 일별 의사결정 로직

1. 유니버스 5종목의 `peerDiscount`를 계산하고, 그중 **최소값(가장 저평가)** 종목을 `bestCandidate`로 선정.
2. **무포지션 상태**라면:
   - `bestCandidate.peerDiscount ≤ entryThreshold` → **ENTRY**(bestCandidate 매수)
   - 아니면 **HOLD**(현금 유지)
3. **포지션 보유 중**이라면, 아래 순서로 판정 (SWITCH가 EXIT보다 우선):
   - **SWITCH** 조건: `bestCandidate ≠ 보유종목` **AND** `bestCandidate.peerDiscount ≤ entryThreshold` **AND** `bestCandidate.peerDiscount ≤ 보유종목.peerDiscount - switchThreshold`
     → 보유종목 매도 + bestCandidate 매수
   - 위 조건 불충족 시 **EXIT** 조건: `보유종목.peerDiscount ≥ exitThreshold`
     → 보유종목 매도, 현금 보유
   - 둘 다 아니면 **HOLD**

의사코드:
```
best = argmin(peerDiscount) over universe

if no_position:
    return ENTRY(best) if best.discount <= entryThreshold else HOLD

else:  # holding `cur`
    if best != cur and best.discount <= entryThreshold and best.discount <= cur.discount - switchThreshold:
        return SWITCH(sell=cur, buy=best)
    if cur.discount >= exitThreshold:
        return EXIT(sell=cur)
    return HOLD
```

## 7. 포지션 규모 / 자금운용

- 이전 모델의 `buyUnitRatio`(분할 매수 비율) 개념은 이번 개편에서 완전히 제거됨.
- 초기 전략자금은 **USD 1,000**.
- `maxPositions=1`만 존재하며 동시에 1개 종목만 보유.
- ENTRY 시 가용 현금 100%를 BUY 후보 금액으로 사용.
- SWITCH 시 기존 보유 종목 전량 SELL 후보를 먼저 만들고, `availableCash + currentHoldingMarketValue`를 신규 BUY 후보 금액으로 사용.
- EXIT 시 기존 보유 종목 전량 SELL 후보만 만든다.

⚠️ **확인 필요**: 현금 보유 기간(EXIT 후 재진입 전) 동안 이자수익을 가정할지 여부.

## 8. 주문 체결 가정 — ⚠️ 확인 필요

- 주문 사유(`order_reason`)는 `ENTRY` / `SWITCH` / `EXIT` 세 종류만 존재 (기존 BUY1/2/3 스테이지 개념 삭제됨).
- 체결 가격/체결 시점(당일 종가 vs 익일 시가 등)은 시스템 코드에 정의돼 있지 않음.
- 거래비용(수수료·슬리피지·세금) 반영 여부도 시스템 미정의.
- 백테스트 목적에 맞게 명시적으로 가정하고 문서에 남겨야 함 (제미나이에게도 어떤 가정인지 함께 전달 권장).

## 9. 관련 DB 스키마 (참고용 — 데이터 포맷 정렬 목적)

- **valuation_snapshot**: trading_date, ticker, close_price, ttm_eps, current_per, five_year_average_per, normalized_per, peer_average_normalized_per, peer_discount, strategy_version
- **position_state**: ticker, status(HOLDING만 존재), quantity, average_price, invested_amount, strategy_version(전략당 1행 유니크), opened_at, updated_at
- **order_history / trade_history**: side(BUY/SELL), order_reason(ENTRY/SWITCH/EXIT), 요청/체결 수량·금액, ordered_at/executed_at
- **per_normalization_baseline**: ticker, base_month, five_year_average_per, sample_count, calculated_at (5년 평균 PER 산출 결과 저장용)
- **benchmark_snapshot**: benchmark_symbol, snapshot_date, close_price — 성과 비교용으로 스키마만 추가됨, 현재 전략 로직에서는 아직 사용되지 않음
- **strategy_config**: strategy_version, entry_threshold, switch_threshold, exit_threshold, max_positions, enabled

## 10. 백테스트 산출 지표 (제안)

- 누적 수익률, CAGR
- MDD(최대낙폭)
- Sharpe / Sortino 비율
- 벤치마크 대비 초과수익(알파)
- ENTRY/SWITCH/EXIT 발생 횟수, 평균 보유기간
- 승률(수익으로 마감된 보유 구간 비율)

## 11. 제미나이에 전달 전 결정해야 할 미확정 사항

1. 백테스트 기간
2. 체결가/체결시점 가정
3. 거래비용·세금 반영 여부
4. 현금 보유 기간의 이자수익 반영 여부
5. 5년 평균 PER 산출 방법 (롤링 방식인지, `per_normalization_baseline`의 `base_month` 기준 월별 스냅샷인지)
