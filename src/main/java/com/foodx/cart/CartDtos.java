package com.foodx.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public final class CartDtos {
    private CartDtos() {
    }

    public record AddItemRequest(@NotNull UUID restaurantId,
            @NotNull UUID menuItemId,
            @Min(1) int quantity) {
    }

    public record UpdateQuantityRequest(@Min(1) int quantity) {
    }

    public record CartItemResponse(UUID id, UUID menuItemId, String menuItemName, int quantity) {
    }

    public record CartResponse(UUID id, UUID restaurantId, UUID userId, List<CartItemResponse> items) {
    }
}
