package com.foodx.review;

import com.foodx.common.ReviewStatus;

import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        UUID restaurantId,
        int rating,
        String comment,
        ReviewStatus status) {
}
