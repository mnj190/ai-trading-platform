create schema if not exists trading;

create table trading.strategy_config (
    id bigserial primary key,
    strategy_version varchar(64) not null,
    buy1_threshold numeric(8, 4) not null,
    buy2_threshold numeric(8, 4) not null,
    buy3_threshold numeric(8, 4) not null,
    buy_unit_ratio numeric(8, 4) not null,
    sell_threshold numeric(8, 4) not null,
    enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uk_strategy_config_version unique (strategy_version),
    constraint chk_strategy_config_buy_order check (
        buy3_threshold <= buy2_threshold
        and buy2_threshold <= buy1_threshold
    ),
    constraint chk_strategy_config_buy_unit_ratio check (
        buy_unit_ratio > 0
        and buy_unit_ratio <= 1
    )
);

insert into trading.strategy_config (
    strategy_version,
    buy1_threshold,
    buy2_threshold,
    buy3_threshold,
    buy_unit_ratio,
    sell_threshold,
    enabled
) values (
    'PE_MEAN_REVERSION_V1',
    -0.1500,
    -0.2000,
    -0.2500,
    0.1000,
    0.0000,
    true
);

