package com.foodx.order;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID menuItemId,
        String itemName,
        int quantity,
        long unitPriceMinor,
        long lineTotalMinor) {
}
