package com.foodx.menu;

import com.foodx.common.MenuItemStatus;

import java.util.UUID;

public record ItemResponse(
        UUID id,
        UUID restaurantId,
        UUID categoryId,
        String name,
        String description,
        long priceMinor,
        MenuItemStatus status) {
}
