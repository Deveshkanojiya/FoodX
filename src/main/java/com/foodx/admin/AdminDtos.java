package com.foodx.admin;

import com.foodx.common.AccountStatus;
import com.foodx.common.OrderStatus;
import com.foodx.common.RestaurantStatus;

import java.util.UUID;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record UserView(UUID id, String email, String fullName, String role, AccountStatus status) {
    }

    public record RestaurantView(UUID id, String name, String city, RestaurantStatus status) {
    }

    public record OrderView(UUID id, String orderNumber, OrderStatus status, long totalMinor) {
    }
}
