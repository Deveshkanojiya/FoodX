package com.foodx.menu;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateItemRequest(
        @NotNull(message = "Category ID is required") UUID categoryId,
        @NotBlank(message = "Item name is required") @Size(min = 2, max = 140, message = "Item name must be between 2 and 140 characters") String name,
        @NotBlank(message = "Item description is required") @Size(max = 2000, message = "Item description cannot exceed 2000 characters") String description,
        @Min(value = 0, message = "Price cannot be negative") long priceMinor) {
}
