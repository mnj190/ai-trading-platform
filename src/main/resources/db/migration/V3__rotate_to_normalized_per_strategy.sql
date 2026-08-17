alter table trading.strategy_config
    drop constraint if exists chk_strategy_config_buy_order,
    drop constraint if exists chk_strategy_config_buy_unit_ratio;

alter table trading.strategy_config
    rename column buy1_threshold to entry_threshold;

alter table trading.strategy_config
    rename column sell_threshold to exit_threshold;

alter table trading.strategy_config
    add column switch_threshold numeric(8, 4) not null default 0.0500,
    add column max_positions integer not null default 1;

update trading.strategy_config
set entry_threshold = -0.1500,
    switch_threshold = 0.0500,
    exit_threshold = 0.0000,
    max_positions = 1
where strategy_version = 'PE_MEAN_REVERSION_V1';

alter table trading.strategy_config
    drop column buy2_threshold,
    drop column buy3_threshold,
    drop column buy_unit_ratio,
    drop column updated_at;

alter table trading.strategy_config
    add constraint chk_strategy_config_max_positions check (max_positions = 1),
    add constraint chk_strategy_config_switch_threshold check (switch_threshold > 0),
    add constraint chk_strategy_config_entry_threshold check (entry_threshold < exit_threshold);

create table trading.per_normalization_baseline (
    id bigserial primary key,
    ticker varchar(16) not null,
    base_month date not null,
    five_year_average_per numeric(12, 4) not null,
    sample_count integer not null,
    calculated_at timestamptz not null,
    created_at timestamptz not null default now(),
    constraint uk_per_normalization_baseline unique (ticker, base_month),
    constraint chk_per_normalization_baseline_average check (five_year_average_per > 0),
    constraint chk_per_normalization_baseline_sample_count check (sample_count > 0)
);

alter table trading.valuation_snapshot
    drop constraint if exists chk_valuation_snapshot_peer_average_per;

alter table trading.valuation_snapshot
    rename column peer_average_per to peer_average_normalized_per;

alter table trading.valuation_snapshot
    add column five_year_average_per numeric(12, 4) not null default 1.0000,
    add column normalized_per numeric(12, 4) not null default 1.0000;

alter table trading.valuation_snapshot
    alter column five_year_average_per drop default,
    alter column normalized_per drop default,
    add constraint chk_valuation_snapshot_five_year_average_per check (five_year_average_per > 0),
    add constraint chk_valuation_snapshot_normalized_per check (normalized_per > 0),
    add constraint chk_valuation_snapshot_peer_average_normalized_per check (peer_average_normalized_per > 0);

alter table trading.position_state
    drop constraint if exists uk_position_state_ticker_strategy,
    drop constraint if exists chk_position_state_state;

alter table trading.position_state
    add column status varchar(16) not null default 'HOLDING',
    add column average_price numeric(19, 4) not null default 0.0000,
    add column opened_at timestamptz not null default now();

alter table trading.position_state
    drop column state,
    drop column created_at;

alter table trading.position_state
    alter column average_price drop default,
    add constraint uk_position_state_strategy unique (strategy_version),
    add constraint chk_position_state_status check (status in ('HOLDING')),
    add constraint chk_position_state_average_price check (average_price >= 0);

alter table trading.order_history
    drop constraint if exists chk_order_history_stage;

alter table trading.order_history
    add column order_reason varchar(16) not null default 'ENTRY';

alter table trading.order_history
    drop column stage;

alter table trading.order_history
    alter column order_reason drop default,
    add constraint chk_order_history_order_reason check (order_reason in ('ENTRY', 'SWITCH', 'EXIT'));

alter table trading.trade_history
    drop constraint if exists chk_trade_history_stage;

alter table trading.trade_history
    add column order_reason varchar(16) not null default 'ENTRY';

alter table trading.trade_history
    drop column stage;

alter table trading.trade_history
    alter column order_reason drop default,
    add constraint chk_trade_history_order_reason check (order_reason in ('ENTRY', 'SWITCH', 'EXIT'));

create table trading.benchmark_snapshot (
    id bigserial primary key,
    benchmark_symbol varchar(16) not null,
    snapshot_date date not null,
    close_price numeric(19, 4) not null,
    created_at timestamptz not null default now(),
    constraint uk_benchmark_snapshot unique (benchmark_symbol, snapshot_date),
    constraint chk_benchmark_snapshot_close_price check (close_price > 0)
);

