package com.foodx.restaurant;

import com.foodx.common.RestaurantStatus;

import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        UUID ownerId,
        String name,
        String slug,
        String description,
        String city,
        String address,
        RestaurantStatus status,
        boolean openNow) {
}
