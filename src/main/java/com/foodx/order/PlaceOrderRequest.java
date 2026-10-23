package com.foodx.order;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PlaceOrderRequest(@NotNull(message = "Cart ID is required") UUID cartId) {
}
