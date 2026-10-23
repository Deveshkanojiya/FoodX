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
    public List<UserView> getUsers() {
        return adminService.getUsers();
    }

    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void suspendUser(@PathVariable UUID userId) {
        adminService.suspendUser(userId);
    }

    @GetMapping("/restaurants")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RestaurantView> getRestaurants() {
        return adminService.getRestaurants();
    }

    @PostMapping("/restaurants/{restaurantId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void disableRestaurant(@PathVariable UUID restaurantId) {
        adminService.disableRestaurant(restaurantId);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public List<OrderView> getOrders() {
        return adminService.getOrders();
    }
}
