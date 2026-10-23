package com.foodx.admin;

import com.foodx.common.OrderStatus;

import java.util.UUID;

public record OrderView(
        UUID id,
        String orderNumber,
        OrderStatus status,
        long totalMinor) {
}
