package com.foodx.restaurant;

import com.foodx.common.RestaurantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class RestaurantDtos {
    private RestaurantDtos() {
    }

    public record CreateRestaurantRequest(
            @NotBlank @Size(max = 140) String name,
            @NotBlank @Size(max = 140) String slug,
            @Size(max = 1000) String description,
            @NotBlank String city,
            @NotBlank String address) {
    }

    public record UpdateRestaurantRequest(
            @NotBlank @Size(max = 140) String name,
            @NotBlank @Size(max = 140) String slug,
            @Size(max = 1000) String description,
            @NotBlank String city,
            @NotBlank String address) {
    }

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
}
