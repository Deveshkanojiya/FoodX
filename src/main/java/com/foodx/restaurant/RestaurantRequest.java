package com.foodx.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestaurantRequest(
        @NotBlank(message = "Restaurant name is required") @Size(min = 2, max = 140, message = "Restaurant name must be between 2 and 140 characters") String name,
        @NotBlank(message = "Slug is required") @Size(min = 2, max = 140, message = "Slug must be between 2 and 140 characters") String slug,
        @Size(max = 1000, message = "Description cannot exceed 1000 characters") String description,
        @NotBlank(message = "City is required") @Size(max = 100, message = "City cannot exceed 100 characters") String city,
        @NotBlank(message = "Address is required") @Size(max = 200, message = "Address cannot exceed 200 characters") String address) {
}
