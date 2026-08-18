# KIS Smoke Test

실제 KIS API key를 발급받은 뒤 계좌 조회부터 확인하기 위한 수동 테스트 절차.

## 원칙

- API key와 app secret은 코드, YAML, Markdown에 저장하지 않는다.
- local profile은 기본적으로 KIS 모의투자 도메인을 사용한다.
- `kis-smoke` profile은 계좌 조회 확인용 runner만 실행하기 위한 profile이다.

## 방법 1: 로컬 secret YAML

프로젝트 루트의 `config/application-secret.yaml`에 값을 넣는다.

```yaml
kis:
  api:
    app-key: "발급받은_app_key"
    app-secret: "발급받은_app_secret"
    account-number: "계좌번호_앞8자리"
    account-product-code: "01"
    paper-trading: true
```

`config/application-secret.yaml`은 `.gitignore`에 포함되어 GitHub에 올라가지 않는다.

모의투자용 key는 프로젝트 루트의 `config/application-paper-secret.yaml`에 따로 넣는다. 이 파일도 `.gitignore`에 포함되어 GitHub에 올라가지 않는다.

실행:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,secret,kis-smoke --spring.main.web-application-type=none'
```

모의투자용 현재잔고 조회:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,paper-secret,kis-present-balance-smoke --spring.main.web-application-type=none'
```

모의투자용 주문 요청/체결 조회 smoke:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,paper-secret,kis-paper-order-smoke --spring.main.web-application-type=none --kis.smoke.order.symbol=AAPL --kis.smoke.order.quantity=1 --kis.smoke.order.max-notional-amount=500'
```

이 runner는 `kis.api.paper-trading=true`일 때만 실행된다. 기본 주문은 AAPL 1주 지정가 매수이며, 현재가에서 1% buffer를 더한 가격을 limit price로 사용한다. `kis.smoke.order.limit-price`를 지정하면 해당 가격을 사용한다.

5종목 현재가상세 조회 smoke:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,paper-secret,kis-universe-price-detail-smoke --spring.main.web-application-type=none --kis.smoke.strategy.symbols=NVDA,GOOGL,AAPL,AMZN,MSFT'
```

5종목 valuation 계산/저장 smoke:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,paper-secret,kis-strategy-valuation-smoke --spring.main.web-application-type=none --kis.smoke.strategy.symbols=NVDA,GOOGL,AAPL,AMZN,MSFT --kis.smoke.strategy.base-month=2026-08'
```

`kis-strategy-valuation-smoke`는 기본적으로 `trading.per_normalization_baseline`에 5종목의 `base_month`별 5년 평균 PER이 있어야 실행된다. baseline 없이 DB 저장 경로만 확인해야 할 때는 아래처럼 smoke 전용 fallback을 명시적으로 켠다.

```bash
./gradlew bootRun --args='--spring.profiles.active=local,paper-secret,kis-strategy-valuation-smoke --spring.main.web-application-type=none --kis.smoke.strategy.symbols=NVDA,GOOGL,AAPL,AMZN,MSFT --kis.smoke.strategy.base-month=2026-08 --kis.smoke.strategy.allow-current-per-baseline=true --kis.smoke.strategy.strategy-version=PE_MEAN_REVERSION_V1_SMOKE'
```

이 fallback은 현재 PER을 임시 baseline으로 사용하므로 실제 투자 판단용 할인율이 아니다. 공개 repo에는 계좌별 수치나 secret 값을 기록하지 않는다.

## 운영(prod) DB 기준 실제 일일 평가 실행

`local`/`prod` 두 profile은 서로 다른 DB를 가리킨다. `local`은 기본적으로 `ai_trading_platform` DB(모의투자 스모크 데이터 포함)를 쓰고, `prod`는 `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` 환경변수로 지정한 DB를 쓴다. 운영용으로는 별도 로컬 DB(`ai_trading_platform_prod`)를 만들어 분리한다.

```bash
createdb ai_trading_platform_prod
```

`kis-daily-evaluation` profile은 5종목 현재가상세를 실제로 조회해서 `DailyEvaluationService`로 valuation/전략 판단/주문 요청까지 한 번에 실행하고 DB에 저장한다. `trading.per_normalization_baseline`이 해당 `base_month`에 미리 적재돼 있어야 한다.

```bash
DB_URL="jdbc:postgresql://127.0.0.1:5432/ai_trading_platform_prod" DB_USERNAME="$USER" DB_PASSWORD="" \
./gradlew bootRun --args='--spring.profiles.active=prod,secret,kis-daily-evaluation --spring.main.web-application-type=none'
```

- `secret` profile은 `config/application-secret.yaml`의 실전 KIS 앱키를 쓴다 (읽기 전용 시세 조회만 하므로 안전하다. 실제 주문 제출은 `OrderExecutionSafetyGuard`가 `trading.execution.allow-real-trading=true` 없이는 막는다).
- `tradingDate`는 `America/New_York` 기준 오늘 날짜로 계산한다 — 한국 시간 오전에 실행하면 자연스럽게 "어제 미국 장" 날짜가 된다.
- `kis.evaluation.available-cash` (기본 1000.0000), `kis.evaluation.strategy-version`, `kis.evaluation.symbols`로 조정 가능하다.

`kis-daily-evaluation`은 1회 실행하고 종료하는 수동 profile이다. 매일 자동으로 돌게 하려면 아래 `kis-server` profile을 쓴다.

## 상시 실행 (자동 스케줄러)

`kis-server` profile은 프로세스를 종료하지 않고 계속 떠 있으면서, 매일 `KisDailyTradingService.runOnce()`(평가 → 요청 → 제출까지 전체)를 자동으로 실행한다. 내부적으로 `kis-daily-evaluation`과 완전히 같은 로직(`KisDailyTradingService`)을 쓴다 — 트리거 방식만 수동 vs 스케줄이다.

```bash
DB_URL="jdbc:postgresql://127.0.0.1:5432/ai_trading_platform_prod" DB_USERNAME="$USER" DB_PASSWORD="" \
./gradlew bootRun --args='--spring.profiles.active=prod,secret,kis-server --spring.main.web-application-type=none'
```

- 기본 실행 시각은 매일 07:30(Asia/Seoul)이다. `kis.evaluation.schedule-cron`으로 변경 가능하다 (cron 표현식).
- 미국 장이 없는 날(주말 등)에도 매일 실행되지만, 안전하게 무해한 no-op이 된다 — `valuation_snapshot`은 같은 날짜로 덮어써지고, `OrderRequestService`의 in-flight 주문 가드가 중복 주문 생성을 막는다.
- 한 번의 스케줄 실행이 실패해도(KIS 오류, 네트워크 등) 예외를 잡아서 로그만 남기고, 프로세스는 계속 떠 있다가 다음날 다시 시도한다.
- 이 프로세스를 계속 실행 상태로 두는 것 자체가 "시스템 가동"이다 — 별도의 on/off 스위치는 없고, 프로세스를 켜두면 매일 자동으로 평가·주문까지 실행된다.

## 방법 2: 환경변수

터미널 세션에서만 값을 넣고 싶으면 환경변수를 사용한다.

```bash
export KIS_APP_KEY="발급받은_app_key"
export KIS_APP_SECRET="발급받은_app_secret"
export KIS_ACCOUNT_NUMBER="계좌번호_앞8자리"
export KIS_ACCOUNT_PRODUCT_CODE="01"
export KIS_PAPER_TRADING="true"
```

실행:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,kis-smoke --spring.main.web-application-type=none'
```

## 실전투자 도메인

실전투자를 테스트할 때만 아래 값을 사용한다.

```yaml
kis:
  api:
    base-url: "https://openapi.koreainvestment.com:9443"
    paper-trading: false
```

초기에는 반드시 `paper-trading: true`로 계좌 조회부터 확인한다.

## 자동 주문 실행 안전장치

자동 주문 제출은 `OrderExecutionSafetyGuard`를 통과해야 한다.

기본값:

```yaml
trading:
  execution:
    enabled: false
    allow-real-trading: false
    max-order-notional-amount: 500.0000
```

- `trading.execution.enabled=false`이면 모의/실전 모두 주문 제출이 차단된다.
- `kis.api.paper-trading=false`인 실전 주문은 `trading.execution.allow-real-trading=true`가 추가로 필요하다.
- 주문 수량과 limit price를 곱한 금액이 `max-order-notional-amount`를 넘으면 제출이 차단된다.
- 조회 API는 이 설정과 별개로 사용할 수 있다.

## 계좌 조회 결과

성공하면 로그에 다음 순서가 보인다.

```text
Starting KIS balance smoke test
KIS balance response: returnCode=0, ...
KIS balance holdings: ...
KIS balance summary: ...
KIS balance smoke test finished
```

## 다음 확인

- token 발급 성공 여부
- 계좌 잔고 조회 `returnCode=0` 여부
- `output1` 보유 종목 구조
- `output2` 계좌 요약 구조

이 응답 구조를 확인한 뒤 실제 체결 조회 응답을 `trade_history`에 매핑한다.

## Token Cache

- KIS access token은 `KisAccessTokenProvider`가 관리한다.
- 한 번 발급받은 token은 `expires_in` 기준 유효기간 동안 재사용한다.
- 만료 5분 전부터는 새 token을 발급받는다.
- KIS token 발급은 1분당 1회 제한이 있으므로, runner나 service는 `KisTokenClient`를 직접 호출하지 않고 provider를 사용한다.
- token cache는 기본적으로 `tmp/kis-access-token-cache.properties`에 저장된다.
- cache key에는 app key 원문을 저장하지 않고, base url/app key/account/paper 여부를 묶은 SHA-256 hash만 저장한다.
- 파일 하나에 cache key별로 여러 슬롯을 저장한다 (`<cacheKey>.accessToken` 형태). 모의투자/실전처럼 서로 다른 설정을 오가며 실행해도 서로의 캐시를 덮어쓰지 않는다.
- `tmp/`는 `.gitignore` 대상이므로 token cache 파일은 GitHub에 올라가지 않는다.

## 모의투자 주문 Smoke 결과 기록 원칙

- 공개 repo 문서에는 계좌별 주문가능금액, 수량, 잔고 같은 응답 수치를 기록하지 않는다.
- 실행 결과는 성공/실패 여부와 KIS error code 정도만 남긴다.
- 상세 수치는 개인 Obsidian vault에만 기록한다.
- 2026-08-17 기준 현재가/매수가능금액/체결조회 API는 호출 가능했고, 주문 요청 POST는 KIS gateway routing error `EGW00202`로 실패했다.
- 2026-08-18 기준 해외주식 현재가상세 API는 5종목 모두 호출 가능했고, 응답에서 가격/PER/EPS 필드를 읽을 수 있었다.
- 2026-08-18 기준 valuation 계산/저장 경로는 smoke strategy version으로 확인됐다.
- 실제 전략 할인율 계산에는 `per_normalization_baseline`의 5년 평균 PER 데이터가 필요하다.

## 1000원 주문 테스트 주의

- 현재 V1 주문 client는 해외주식 일반 주문 API를 사용한다.
- 일반 미국 주식 주문은 우선 1주 단위 수량 계산을 기준으로 한다.
- 1000원은 USD 기준으로 1달러 미만 수준이라 NVDA, GOOGL, AAPL, AMZN, MSFT 같은 V1 유니버스 종목은 수량이 0이 된다.
- 따라서 실제 주문 테스트 전에는 현재가 조회와 주문 가능 금액 조회로 `quantity > 0`인지 먼저 확인해야 한다.
- `paper-trading: true`일 때는 반드시 모의투자용 app key를 사용해야 한다.
- 실전용 app key를 넣었다면 `paper-trading: false`와 실전 base url이 맞지만, 실제 주문은 별도 승인 없이 실행하지 않는다.
