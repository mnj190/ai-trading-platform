# 공개 포트폴리오 대시보드 설계 문서

## 배경 및 목적

현재 이 프로젝트는 KIS API로 `PE_MEAN_REVERSION_V1` 전략을 자동매매하는 백엔드만 존재하고, 화면이 전혀 없다(`static`/`templates` 비어있음, `api` 패키지도 placeholder뿐).

이 결과를 로그인 없이 누구나 볼 수 있는 공개 웹페이지로 만든다. 목적은 이 시스템이 실제로 정해진 규칙대로 작동하고 있다는 것을 투명하게 보여주는 것이다.

## 범위

- 인증/로그인 없음. 관리자 기능 없음 — 읽기 전용 공개 사이트.
- 잔고/수익률은 실제 달러 금액을 그대로 노출한다(비율만 보여주는 방식이 아니다).
- 기존 데이터만 사용 — 새 테이블/엔티티는 만들지 않는다. 5개 화면 모두 이미 존재하는 엔티티/리포지토리로 구현 가능하다.
- 기술 스택: Spring Boot + Thymeleaf 서버사이드 렌더링, 기존 프로젝트와 같은 배포 단위. 별도 SPA/REST API를 만들지 않는다.

## 화면 스펙

### 1. 개요 / 전략 소개 (`/`)

정적 설명 + `strategy_config` 라이브 조회.

- 전략명, 유니버스 5종목(NVDA/GOOGL/AAPL/AMZN/MSFT) — 정적 텍스트
- 핵심 로직 설명(자기 5년 평균 PER 대비 정규화 → 그룹 평균보다 가장 저평가된 1종목만 보유, ENTRY/SWITCH/EXIT 규칙) — 정적 텍스트
- 현재 라이브 설정값 — `StrategyConfigRepository.findByStrategyVersion("PE_MEAN_REVERSION_V1")`: `entryThreshold`, `switchThreshold`, `exitThreshold`, `maxPositions`
- 시작 자본 $1,000 — 정적 텍스트

### 2. 현재 잔고 / 보유 현황 (`/portfolio`)

- 최신 `AccountSnapshot` 1건 — `AccountSnapshotRepository`에 최신 스냅샷 조회 메서드 신규 추가 필요(`findTopByOrderBySnapshotDateDesc` 등)
  - `totalEquity`, `cashBalance`, `stockMarketValue`, `unrealizedPnl`, `snapshotDate`, `currency`
- 현재 보유 종목 — `PositionStateRepository.findByStrategyVersion(strategyVersion)`(기존 메서드)
  - 비어있으면 "현재 현금 보유 중(무포지션)" 표시
  - 있으면: `ticker`, `quantity`, `averagePrice`, `investedAmount`, `openedAt`
- 보유종목 평가금액은 별도 계산 없이 `AccountSnapshot.stockMarketValue`를 그대로 사용한다(`maxPositions=1`이라 항상 해당 보유종목 하나의 평가금액과 같음).
- `AccountSnapshot`이 아직 하나도 없으면("첫 실행 전") 빈 상태 문구를 표시한다.

### 3. 벤치마크(QQQM/SPY) 대비 수익률 (`/performance`)

- `AccountSnapshot` 전체를 `snapshotDate` 오름차순으로 — `AccountSnapshotRepository`에 신규 메서드 추가 필요(`findAllByOrderBySnapshotDateAsc` 등)
- `BenchmarkSnapshot`을 SPY, QQQM 각각 `snapshotDate` 오름차순으로 — `BenchmarkSnapshotRepository`에 신규 메서드 추가 필요(`findByBenchmarkSymbolOrderBySnapshotDateAsc` 등)
- 각 시계열의 첫 값을 0%로 정규화한 누적수익률을 계좌/SPY/QQQM 3개 라인으로 그래프에 표시. 그래프 렌더링 방식(라이브러리 선택 포함)은 비주얼 디자인 단계에서 구체화한다 — 여기서는 "정규화된 누적수익률 계산과 시계열 데이터가 필요하다"까지만 확정한다.
- 그래프 아래 요약: 계좌/SPY/QQQM 각각 현재 누적수익률 %
- 스냅샷이 1건 이하면 그래프 대신 "데이터 축적 중" 문구를 표시한다.

### 4. 매수/매도 현황 (`/orders`)

- `OrderHistoryRepository`로 전략의 주문 이력을 최신순으로 조회(기존 `findByStrategyVersionOrderByOrderedAtAsc`를 역순 정렬하거나, `...OrderByOrderedAtDesc` 신규 메서드 추가)
- 각 주문에 대해 `TradeHistoryRepository.findByOrder_IdOrderByExecutedAtAsc`로 체결 내역을 결합한다(v1 단계는 주문 건수가 적으므로 N+1 조회를 허용하고, 필요해지면 나중에 최적화한다)
- 표시 필드: `orderedAt`, `ticker`, `side`, `orderReason`, `requestedAmount`, `status`, 체결된 경우 `executedPrice`/`executedQuantity`/`executedAmount`/`executedAt`
- 주문 이력이 없으면 "아직 주문 없음" 빈 상태를 표시한다.

### 5. 시스템 상태 (`/status`)

- 마지막 평가일 — `ValuationSnapshotRepository`에 신규 메서드 추가 필요(`findTopByStrategyVersionOrderByTradingDateDesc` 등), `tradingDate` 표시
- 마지막 주문 활동 — 4번 화면과 같은 `OrderHistory` 목록에서 가장 최근 `orderedAt`
- "정상 작동 중" 같은 간단한 상태 텍스트

## 아키텍처

- 새 `com.mnj190.aitrading.web` 패키지를 기존 `api`/`broker`/`market`/`order`/`portfolio`/`strategy`와 같은 레벨에 추가한다.
- 화면별 `@Controller` 5개(`OverviewController`, `PortfolioController`, `PerformanceController`, `OrdersController`, `StatusController`). 각 컨트롤러는 필요한 리포지토리를 생성자로 주입받아 조회하고, 결과를 Thymeleaf `Model`에 담아 뷰 이름을 반환한다.
- `src/main/resources/templates/`: 화면별 템플릿 5개 + 공통 레이아웃(상단 네비게이션, 5개 화면 링크로 이동) fragment 1개.
- `src/main/resources/static/`: CSS(및 필요시 차트 렌더링용 JS) — 공개 페이지이므로 외부 CDN에 의존하지 않고 self-host한다.
- v1 규모에서는 컨트롤러가 리포지토리를 직접 호출하고 별도 서비스 레이어를 두지 않는다. 화면별 조회 로직이 복잡해지면 그때 서비스로 추출한다.
- 신규 리포지토리 메서드 4개(위 화면 스펙에 명시), 나머지는 기존 메서드를 그대로 재사용한다.

## 에러 / 빈 상태 처리

전략이 최근에 시작되어 데이터가 아직 거의 없다. 모든 화면은 "데이터 없음"을 예외가 아니라 정상적인 화면 상태로 처리해야 한다.

- `AccountSnapshot` 0건 → 잔고/수익률 화면에 안내 문구
- `PositionState` 없음 → 무포지션 안내
- `OrderHistory` 없음 → 주문 없음 안내
- `BenchmarkSnapshot` 1건 이하 → 그래프 대신 "데이터 축적 중" 안내

## 테스트 전략

- 컨트롤러별 `@SpringBootTest` + `MockMvc`로 200 응답, 뷰 이름, 모델 속성을 검증한다.
- 데이터가 없는 경우/있는 경우 두 시나리오를 모두 테스트한다.
- 신규 리포지토리 메서드는 기존 리포지토리 테스트 패턴(`@SpringBootTest` + `@Transactional`, 예: `OrderHistoryRepositoryTests`)을 따른다.

## Out of Scope (v1 제외)

- 로그인/인증, 관리자 기능(주문 승인/취소 등)
- 성과 지표(CAGR/MDD/Sharpe/승률) 계산 및 표시
- 종목별 히스토리 타임라인 화면
- 별도 SPA/REST API
- 반응형/모바일 레이아웃 세부사항(비주얼 디자인 단계에서 결정)
- 그래프 라이브러리 선택 및 시각적 스타일(비주얼 디자인 단계에서 결정)

## 다음 단계

화면 레이아웃/비주얼 디자인(와이어프레임, 색상, 배치)을 먼저 정의한 뒤, `superpowers:writing-plans` 스킬로 구현 계획을 작성한다.
