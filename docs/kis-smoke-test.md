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

실행:

```bash
./gradlew bootRun --args='--spring.profiles.active=local,secret,kis-smoke --spring.main.web-application-type=none'
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
