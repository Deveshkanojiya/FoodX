package com.foodx.admin;

import com.foodx.common.RestaurantStatus;

import java.util.UUID;

public record RestaurantView(
        UUID id,
        String name,
        String city,
        RestaurantStatus status) {
}
