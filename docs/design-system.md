# 디자인 시스템 (미니멀 클린 / 카드형 대시보드)

이 문서는 공개 포트폴리오 대시보드의 비주얼 언어(색상/타이포그래피/레이아웃/컴포넌트)를 정의한다. 목적은 두 가지다.

1. 이번 프로젝트(현재 `PE_MEAN_REVERSION_V1` 화면 5개)의 실제 구현 기준.
2. **재사용성** — 앞으로 만들 다른 AI 포트폴리오 프로젝트는 별도 서버/프로젝트로 만들지만, 이 문서의 색상·타이포·컴포넌트 토큰만 그대로 가져가면 같은 톤앤매너를 유지할 수 있게 한다. 그래서 값은 전부 CSS custom property(디자인 토큰) 형태로 정의한다.

톤: **미니멀 클린, 라이트 모드, 여백 많은 카드형 SaaS 대시보드.** 다크 모드는 v1 범위 아님(토큰 구조는 나중에 다크 값만 추가하면 확장 가능하도록 잡아둔다).

## 1. 색상 토큰

```css
:root {
  /* 배경 */
  --color-bg: #FFFFFF;           /* 페이지 배경 */
  --color-bg-subtle: #F8FAFC;    /* 카드 바깥 섹션 배경 */
  --color-surface: #FFFFFF;      /* 카드 배경 */
  --color-border: #E2E8F0;       /* 카드 보더, 구분선 */

  /* 텍스트 */
  --color-text-primary: #0F172A;   /* 본문/제목 */
  --color-text-secondary: #64748B; /* 라벨, 보조 텍스트 */
  --color-text-muted: #94A3B8;     /* 빈 상태 문구, placeholder 성격 텍스트 */

  /* 포인트 컬러 */
  --color-accent: #2563EB;
  --color-accent-hover: #1D4ED8;
  --color-accent-subtle-bg: #EFF6FF; /* 포인트 컬러의 옅은 배경(배지 등) */

  /* 손익 시맨틱 컬러 */
  --color-positive: #16A34A;       /* 수익/상승 */
  --color-positive-subtle-bg: #F0FDF4;
  --color-negative: #DC2626;       /* 손실/하락 */
  --color-negative-subtle-bg: #FEF2F2;

  /* 중립 배지(주문 상태 등 기본값) */
  --color-neutral-badge-bg: #F1F5F9;
  --color-neutral-badge-text: #475569;
}
```

**사용 원칙**: `--color-accent`는 버튼/링크/현재 페이지 네비게이션 강조에만 쓴다. 손익 색상(`positive`/`negative`)은 반드시 숫자(수익률, 손익 금액)에만 쓰고 장식 목적으로 쓰지 않는다.

### 차트 시리즈 컬러 (3번 화면 전용)

계좌/SPY/QQQM 3개 라인을 구분해야 하므로, 손익 시맨틱 컬러와 겹치지 않는 별도 팔레트를 쓴다.

```css
--chart-series-account: #2563EB; /* 블루 = 계좌(accent와 동일) */
--chart-series-spy: #64748B;     /* 슬레이트 그레이 = SPY */
--chart-series-qqqm: #D97706;    /* 앰버 = QQQM */
```

## 2. 타이포그래피

외부 폰트 CDN에 의존하지 않는다(공개 사이트이므로 self-host 또는 시스템 폰트만 사용).

```css
--font-family-base: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
```

숫자(금액/수량/퍼센트)는 표에서 자릿수가 흔들리지 않도록 `font-variant-numeric: tabular-nums;`를 적용한다. 별도 모노스페이스 폰트는 쓰지 않는다.

| 용도 | 크기 | 굵기 | 색상 토큰 |
|---|---|---|---|
| 페이지 타이틀 (H1) | 28px | 700 | `--color-text-primary` |
| 섹션 제목 (H2) | 20px | 600 | `--color-text-primary` |
| 본문 | 15px | 400 | `--color-text-primary` |
| 라벨/캡션 | 13px | 500 | `--color-text-secondary` |
| 통계 카드 큰 숫자 | 32px | 700 | 상황별(`--color-text-primary` 기본, 손익이면 시맨틱 컬러) |

## 3. 간격/레이아웃 스케일

4px 기준 스케일: `4 / 8 / 12 / 16 / 24 / 32 / 48px`

- 콘텐츠 최대 너비: **1080px**, 가운데 정렬, 좌우 최소 padding 24px
- 카드 내부 padding: 24px
- 카드 사이 세로 간격: 24px
- 카드 보더: `1px solid var(--color-border)`, `border-radius: 12px`
- 카드 그림자: `0 1px 2px rgba(15, 23, 42, 0.04)` (아주 옅게, 보더가 주된 구분 수단)

## 4. 공통 레이아웃 구조

모든 화면이 공유하는 뼈대:

```
┌─────────────────────────────────────────────┐
│ [전략명/로고]      개요 잔고 수익률 주문 상태  │  ← 상단 네비게이션 (sticky, 64px, 흰 배경 + 하단 보더)
├─────────────────────────────────────────────┤
│                                               │
│   (콘텐츠 영역, max-width 1080px, 카드 스택)   │
│                                               │
└─────────────────────────────────────────────┘
```

- 네비게이션: 좌측에 전략명 텍스트(로고 이미지 없음, v1은 텍스트만), 우측 또는 중앙에 5개 메뉴. 현재 페이지는 `--color-accent`로 강조 + 밑줄.
- 콘텐츠 영역은 항상 카드 단위로 세로 스택. 화면 간 레이아웃 일관성을 위해 그리드를 쓰지 않고 단일 컬럼 스택을 기본으로 한다(모바일 대응이 자연스럽고, v1 데이터 밀도가 낮아 여러 컬럼이 필요 없음).

## 5. 공통 컴포넌트

### 5.1 통계 카드(Stat Card)
라벨(작은 텍스트) + 큰 숫자 + 선택적 델타 배지.

```
┌───────────────────┐
│ 총 자산             │  ← 라벨, --color-text-secondary
│ $1,284.50          │  ← 큰 숫자, 32px/700
│ ▲ +12.4%           │  ← 델타 배지(선택), positive/negative 색상
└───────────────────┘
```

여러 개를 가로로 나열할 때는 균등 폭으로 배치(예: 잔고 화면의 총자산/현금/보유평가금액/평가손익 4개).

### 5.2 배지/필(Badge)
주문 상태(`OrderStatus`)와 주문 사유(`OrderReason`)에 색상을 매핑한다.

| 값 | 배경 | 텍스트 |
|---|---|---|
| `REQUESTED`, `SUBMITTED` | `--color-neutral-badge-bg` | `--color-neutral-badge-text` |
| `PARTIALLY_FILLED` | `--color-accent-subtle-bg` | `--color-accent` |
| `FILLED` | `--color-positive-subtle-bg` | `--color-positive` |
| `CANCELLED`, `REJECTED` | `--color-negative-subtle-bg` | `--color-negative` |
| `ENTRY`, `SWITCH`, `EXIT` (order_reason) | `--color-neutral-badge-bg` | `--color-neutral-badge-text` |

모양: `border-radius: 999px`(완전한 알약형), 좌우 padding 10px, 상하 padding 4px, 13px/500.

### 5.3 데이터 테이블
헤더 행: `--color-bg-subtle` 배경, `--color-text-secondary` 텍스트, 13px/600.
본문 행: 흰 배경, hover 시 `--color-bg-subtle`. 숫자 컬럼은 우측 정렬 + tabular-nums.
상태 컬럼은 5.2 배지 컴포넌트를 그대로 사용.

### 5.4 라인 차트 카드
카드 안에 제목(H2) + 범례(색상 점 + 라벨: 계좌/SPY/QQQM, 4.1의 차트 컬러 사용) + 차트 + 하단에 5.1 통계 카드 스타일의 미니 요약(계좌/SPY/QQQM 각 누적수익률 %).

### 5.5 빈 상태(Empty State)
데이터가 없는 정상 상태를 위한 패턴. 아이콘 없이, 카드 중앙 정렬, `--color-text-muted` 텍스트 한 줄(+ 필요하면 보조 설명 한 줄).

예: "아직 보유 종목이 없습니다 (현금 보유 중)", "데이터를 축적하는 중입니다", "아직 주문 내역이 없습니다"

## 6. 화면별 레이아웃 구성

토큰/컴포넌트를 실제 5개 화면에 어떻게 배치할지 정리한다. (화면별 데이터 필드는 `docs/superpowers/specs/2026-08-19-public-portfolio-dashboard-design.md` 참고)

### 개요 (`/`)
```
[H1: PE_MEAN_REVERSION_V1]
[카드] 전략 설명 (유니버스 5종목, 로직 설명 — 본문 텍스트)
[카드] 현재 설정값 — entry/switch/exit threshold, max positions (라벨+값 리스트)
```

### 잔고 (`/portfolio`)
```
[H1: 현재 잔고]
[통계 카드 4개 가로 배치] 총자산 | 현금 | 보유평가금액 | 평가손익
[카드] 보유 종목 상세 (ticker, 수량, 평균단가, 투자금액, 진입일) — 없으면 5.5 빈 상태
```

### 수익률 비교 (`/performance`)
```
[H1: 벤치마크 대비 수익률]
[5.4 라인 차트 카드] 계좌 vs SPY vs QQQM 누적수익률
스냅샷 1건 이하면 차트 대신 5.5 빈 상태
```

### 매수/매도 현황 (`/orders`)
```
[H1: 매수/매도 현황]
[5.3 데이터 테이블] 주문일시 | 종목 | 방향 | 사유(배지) | 요청금액 | 상태(배지) | 체결가/체결수량/체결일시
없으면 5.5 빈 상태
```

### 시스템 상태 (`/status`)
```
[H1: 시스템 상태]
[통계 카드 2개] 마지막 평가일 | 마지막 주문 활동
[상태 텍스트] "정상 작동 중" (positive 색상 점 + 텍스트)
```

## 7. 반응형

브레이크포인트 1개만 둔다: 640px 미만에서는 통계 카드 가로 나열을 세로 스택으로 전환하고, 네비게이션 메뉴는 가로 스크롤 가능한 한 줄로 유지한다(햄버거 메뉴 없음 — 메뉴가 5개뿐이라 불필요).

## 8. 다른 프로젝트에서 재사용하는 방법

1. 섹션 1(색상 토큰)과 2(타이포그래피 변수)를 그대로 복사한다.
2. 포트폴리오마다 `--color-accent`만 다른 색으로 바꿔서 프로젝트별 아이덴티티를 줄 수 있다(선택 사항 — 그대로 블루를 써도 무방).
3. 섹션 5(공통 컴포넌트)의 스타일 규칙(카드 보더/그림자, 배지 모양, 테이블 스타일)은 그대로 유지해서 여러 포트폴리오 사이트를 넘나들어도 같은 제품군처럼 보이게 한다.
