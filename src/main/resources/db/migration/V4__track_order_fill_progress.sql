alter table trading.order_history
    add column submitted_quantity numeric(19, 6),
    add column filled_quantity numeric(19, 6) not null default 0;

alter table trading.order_history
    add constraint chk_order_history_filled_quantity check (filled_quantity >= 0);
