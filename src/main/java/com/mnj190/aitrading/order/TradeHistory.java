package com.mnj190.aitrading.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trade_history", schema = "trading")
public class TradeHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderHistory order;

	@Column(nullable = false, length = 16)
	private String ticker;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 8)
	private OrderSide side;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_reason", nullable = false, length = 16)
	private OrderReason orderReason;

	@Column(name = "executed_quantity", nullable = false, precision = 19, scale = 6)
	private BigDecimal executedQuantity;

	@Column(name = "executed_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal executedPrice;

	@Column(name = "executed_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal executedAmount;

	@Column(name = "executed_at", nullable = false)
	private OffsetDateTime executedAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected TradeHistory() {
	}

	public TradeHistory(
			OrderHistory order,
			String ticker,
			OrderSide side,
			OrderReason orderReason,
			BigDecimal executedQuantity,
			BigDecimal executedPrice,
			BigDecimal executedAmount,
			OffsetDateTime executedAt
	) {
		this.order = order;
		this.ticker = ticker;
		this.side = side;
		this.orderReason = orderReason;
		this.executedQuantity = executedQuantity;
		this.executedPrice = executedPrice;
		this.executedAmount = executedAmount;
		this.executedAt = executedAt;
	}

	@PrePersist
	void prePersist() {
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public OrderHistory getOrder() {
		return order;
	}

	public String getTicker() {
		return ticker;
	}

	public OrderSide getSide() {
		return side;
	}

	public OrderReason getOrderReason() {
		return orderReason;
	}

	public BigDecimal getExecutedQuantity() {
		return executedQuantity;
	}

	public BigDecimal getExecutedPrice() {
		return executedPrice;
	}

	public BigDecimal getExecutedAmount() {
		return executedAmount;
	}

	public OffsetDateTime getExecutedAt() {
		return executedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
