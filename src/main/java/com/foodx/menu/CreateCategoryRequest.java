package com.foodx.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Category name is required") @Size(min = 2, max = 140, message = "Category name must be between 2 and 140 characters") String name,
        @NotBlank(message = "Category slug is required") @Size(min = 2, max = 140, message = "Category slug must be between 2 and 140 characters") String slug) {
}
