package com.mnj190.aitrading.order;

public record ExecutionSyncReport(
		int receivedFills,
		int recordedFills,
		int skippedUnknownOrders,
		int skippedNonSubmittedOrders,
		int skippedAlreadyUpToDate
) {
}
