package com.mnj190.aitrading.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "order_history", schema = "trading")
public class OrderHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 16)
	private String ticker;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 8)
	private OrderSide side;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_reason", nullable = false, length = 16)
	private OrderReason orderReason;

	@Column(name = "requested_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal requestedAmount;

	@Column(name = "requested_quantity", precision = 19, scale = 6)
	private BigDecimal requestedQuantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "order_type", nullable = false, length = 32)
	private OrderType orderType;

	@Column(name = "broker_order_id", length = 128)
	private String brokerOrderId;

	@Column(name = "submitted_quantity", precision = 19, scale = 6)
	private BigDecimal submittedQuantity;

	@Column(name = "filled_quantity", nullable = false, precision = 19, scale = 6)
	private BigDecimal filledQuantity = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private OrderStatus status;

	@Column(name = "strategy_version", nullable = false, length = 64)
	private String strategyVersion;

	@Column(name = "ordered_at", nullable = false)
	private OffsetDateTime orderedAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected OrderHistory() {
	}

	public OrderHistory(
			String ticker,
			OrderSide side,
			OrderReason orderReason,
			BigDecimal requestedAmount,
			BigDecimal requestedQuantity,
			OrderType orderType,
			OrderStatus status,
			String strategyVersion,
			OffsetDateTime orderedAt
	) {
		this.ticker = ticker;
		this.side = side;
		this.orderReason = orderReason;
		this.requestedAmount = requestedAmount;
		this.requestedQuantity = requestedQuantity;
		this.orderType = orderType;
		this.status = status;
		this.strategyVersion = strategyVersion;
		this.orderedAt = orderedAt;
	}

	public static OrderHistory fromCandidate(OrderCandidate candidate, OffsetDateTime orderedAt) {
		return new OrderHistory(
				candidate.ticker(),
				candidate.side(),
				candidate.reason(),
				candidate.requestedAmount(),
				candidate.requestedQuantity(),
				candidate.orderType(),
				OrderStatus.REQUESTED,
				candidate.strategyVersion(),
				orderedAt
		);
	}

	@PrePersist
	void prePersist() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
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

	public BigDecimal getRequestedAmount() {
		return requestedAmount;
	}

	public BigDecimal getRequestedQuantity() {
		return requestedQuantity;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public String getBrokerOrderId() {
		return brokerOrderId;
	}

	public BigDecimal getSubmittedQuantity() {
		return submittedQuantity;
	}

	public BigDecimal getFilledQuantity() {
		return filledQuantity;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public String getStrategyVersion() {
		return strategyVersion;
	}

	public OffsetDateTime getOrderedAt() {
		return orderedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void markSubmitted(String brokerOrderId, BigDecimal submittedQuantity) {
		if (brokerOrderId == null || brokerOrderId.isBlank()) {
			throw new IllegalArgumentException("brokerOrderId must not be blank");
		}
		if (submittedQuantity == null || submittedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("submittedQuantity must be greater than zero");
		}
		this.brokerOrderId = brokerOrderId;
		this.submittedQuantity = submittedQuantity;
		this.status = OrderStatus.SUBMITTED;
	}

	public BigDecimal applyCumulativeFillDelta(BigDecimal reportedCumulativeFilledQuantity) {
		Objects.requireNonNull(reportedCumulativeFilledQuantity, "reportedCumulativeFilledQuantity must not be null");
		BigDecimal delta = reportedCumulativeFilledQuantity.subtract(filledQuantity);
		if (delta.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		if (status != OrderStatus.SUBMITTED && status != OrderStatus.PARTIALLY_FILLED) {
			throw new IllegalStateException("only SUBMITTED or PARTIALLY_FILLED orders can record new fills");
		}
		filledQuantity = filledQuantity.add(delta);
		status = filledQuantity.compareTo(submittedQuantity) >= 0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
		return delta;
	}

	public void markRejected() {
		this.status = OrderStatus.REJECTED;
	}
}
