create table trading.account_snapshot (
    id bigserial primary key,
    snapshot_date date not null,
    total_equity numeric(19, 4) not null,
    cash_balance numeric(19, 4) not null,
    stock_market_value numeric(19, 4) not null,
    unrealized_pnl numeric(19, 4) not null,
    currency char(3) not null,
    created_at timestamptz not null default now(),
    constraint uk_account_snapshot_date unique (snapshot_date),
    constraint chk_account_snapshot_total_equity check (total_equity >= 0),
    constraint chk_account_snapshot_cash_balance check (cash_balance >= 0),
    constraint chk_account_snapshot_stock_market_value check (stock_market_value >= 0)
);

create table trading.valuation_snapshot (
    id bigserial primary key,
    trading_date date not null,
    ticker varchar(16) not null,
    close_price numeric(19, 4) not null,
    ttm_eps numeric(19, 4) not null,
    current_per numeric(12, 4) not null,
    peer_average_per numeric(12, 4) not null,
    peer_discount numeric(8, 4) not null,
    strategy_version varchar(64) not null,
    created_at timestamptz not null default now(),
    constraint uk_valuation_snapshot unique (trading_date, ticker, strategy_version),
    constraint chk_valuation_snapshot_close_price check (close_price > 0),
    constraint chk_valuation_snapshot_current_per check (current_per > 0),
    constraint chk_valuation_snapshot_peer_average_per check (peer_average_per > 0)
);

create table trading.position_state (
    id bigserial primary key,
    ticker varchar(16) not null,
    state varchar(16) not null,
    quantity numeric(19, 6) not null,
    invested_amount numeric(19, 4) not null,
    strategy_version varchar(64) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uk_position_state_ticker_strategy unique (ticker, strategy_version),
    constraint chk_position_state_state check (state in ('NONE', 'BUY1', 'BUY2', 'BUY3')),
    constraint chk_position_state_quantity check (quantity >= 0),
    constraint chk_position_state_invested_amount check (invested_amount >= 0)
);

create table trading.order_history (
    id bigserial primary key,
    ticker varchar(16) not null,
    side varchar(8) not null,
    stage varchar(16) not null,
    requested_amount numeric(19, 4) not null,
    requested_quantity numeric(19, 6),
    order_type varchar(32) not null,
    broker_order_id varchar(128),
    status varchar(32) not null,
    strategy_version varchar(64) not null,
    ordered_at timestamptz not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint chk_order_history_side check (side in ('BUY', 'SELL')),
    constraint chk_order_history_stage check (stage in ('NONE', 'BUY1', 'BUY2', 'BUY3')),
    constraint chk_order_history_requested_amount check (requested_amount >= 0),
    constraint chk_order_history_requested_quantity check (
        requested_quantity is null
        or requested_quantity >= 0
    )
);

create table trading.trade_history (
    id bigserial primary key,
    order_id bigint not null references trading.order_history (id),
    ticker varchar(16) not null,
    side varchar(8) not null,
    stage varchar(16) not null,
    executed_quantity numeric(19, 6) not null,
    executed_price numeric(19, 4) not null,
    executed_amount numeric(19, 4) not null,
    executed_at timestamptz not null,
    created_at timestamptz not null default now(),
    constraint chk_trade_history_side check (side in ('BUY', 'SELL')),
    constraint chk_trade_history_stage check (stage in ('NONE', 'BUY1', 'BUY2', 'BUY3')),
    constraint chk_trade_history_executed_quantity check (executed_quantity > 0),
    constraint chk_trade_history_executed_price check (executed_price > 0),
    constraint chk_trade_history_executed_amount check (executed_amount > 0)
);

create index idx_valuation_snapshot_ticker_date
    on trading.valuation_snapshot (ticker, trading_date);

create index idx_order_history_broker_order_id
    on trading.order_history (broker_order_id);

create index idx_trade_history_order_id
    on trading.trade_history (order_id);

