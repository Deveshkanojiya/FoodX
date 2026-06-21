package com.foodx.review;

import com.foodx.common.ReviewStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public final class ReviewDtos {
    private ReviewDtos() {
    }

    public record CreateReviewRequest(@NotNull UUID orderId,
            @Min(1) @Max(5) int rating,
            @NotBlank String comment) {
    }

    public record ReviewResponse(UUID id, UUID orderId, UUID userId, UUID restaurantId, int rating, String comment,
            ReviewStatus status) {
    }
}
