package com.foodx.order;

import com.foodx.common.OrderStatus;

import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID userId,
        UUID restaurantId,
        OrderStatus status,
        long totalMinor,
        List<OrderItemResponse> items) {
}
