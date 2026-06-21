package com.foodx.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminDtos.UserView> users() {
        return adminService.users();
    }

    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public void suspendUser(@PathVariable UUID userId) {
        adminService.suspendUser(userId);
    }

    @GetMapping("/restaurants")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminDtos.RestaurantView> restaurants() {
        return adminService.restaurants();
    }

    @PostMapping("/restaurants/{restaurantId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public void disableRestaurant(@PathVariable UUID restaurantId) {
        adminService.disableRestaurant(restaurantId);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminDtos.OrderView> orders() {
        return adminService.orders();
    }
}
