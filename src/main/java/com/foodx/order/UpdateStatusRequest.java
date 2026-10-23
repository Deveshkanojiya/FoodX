package com.foodx.order;

import com.foodx.common.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull(message = "Order status is required") OrderStatus status) {
}
