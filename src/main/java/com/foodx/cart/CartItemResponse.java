package com.foodx.cart;

import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID menuItemId,
        String menuItemName,
        int quantity) {
}
