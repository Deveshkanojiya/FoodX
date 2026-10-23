package com.foodx.menu;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID restaurantId,
        String name,
        String slug,
        boolean active) {
}
