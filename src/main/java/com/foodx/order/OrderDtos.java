package com.foodx.order;

import com.foodx.common.OrderStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record PlaceOrderRequest(@NotNull UUID cartId) {
    }

    public record UpdateStatusRequest(@NotNull OrderStatus status) {
    }

    public record OrderItemResponse(UUID id, UUID menuItemId, String itemName, int quantity, long unitPriceMinor,
            long lineTotalMinor) {
    }

    public record OrderResponse(UUID id, String orderNumber, UUID userId, UUID restaurantId, OrderStatus status,
            long totalMinor, List<OrderItemResponse> items) {
    }
}
