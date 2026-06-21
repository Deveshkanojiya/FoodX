package com.foodx.menu;

import com.foodx.common.MenuItemStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public final class MenuDtos {
    private MenuDtos() {
    }

    public record CreateCategoryRequest(@NotBlank @Size(max = 140) String name,
            @NotBlank @Size(max = 140) String slug) {
    }

    public record CategoryResponse(UUID id, UUID restaurantId, String name, String slug, boolean active) {
    }

    public record CreateItemRequest(@NotNull UUID categoryId,
            @NotBlank @Size(max = 140) String name,
            @NotBlank @Size(max = 2000) String description,
            @Min(0) long priceMinor) {
    }

    public record UpdateItemRequest(@NotNull UUID categoryId,
            @NotBlank @Size(max = 140) String name,
            @NotBlank @Size(max = 2000) String description,
            @Min(0) long priceMinor,
            @NotNull MenuItemStatus status) {
    }

    public record ItemResponse(UUID id, UUID restaurantId, UUID categoryId, String name, String description,
            long priceMinor, MenuItemStatus status) {
    }
}
