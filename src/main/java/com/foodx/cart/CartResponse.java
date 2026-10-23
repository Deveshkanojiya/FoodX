package com.foodx.cart;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID restaurantId,
        UUID userId,
        List<CartItemResponse> items) {
}
