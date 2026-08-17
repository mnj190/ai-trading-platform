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

## 1000원 주문 테스트 주의

- 현재 V1 주문 client는 해외주식 일반 주문 API를 사용한다.
- 일반 미국 주식 주문은 우선 1주 단위 수량 계산을 기준으로 한다.
- 1000원은 USD 기준으로 1달러 미만 수준이라 NVDA, GOOGL, AAPL, AMZN, MSFT 같은 V1 유니버스 종목은 수량이 0이 된다.
- 따라서 실제 주문 테스트 전에는 현재가 조회와 주문 가능 금액 조회로 `quantity > 0`인지 먼저 확인해야 한다.
- `paper-trading: true`일 때는 반드시 모의투자용 app key를 사용해야 한다.
- 실전용 app key를 넣었다면 `paper-trading: false`와 실전 base url이 맞지만, 실제 주문은 별도 승인 없이 실행하지 않는다.
