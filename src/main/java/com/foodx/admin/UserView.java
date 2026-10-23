package com.foodx.admin;

import com.foodx.common.AccountStatus;

import java.util.UUID;

public record UserView(
        UUID id,
        String email,
        String fullName,
        String role,
        AccountStatus status) {
}
