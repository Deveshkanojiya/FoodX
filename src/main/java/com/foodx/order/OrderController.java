package com.foodx.order;

import com.foodx.user.User;
import com.foodx.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public OrderResponse placeOrder(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody PlaceOrderRequest request) {
        return orderService.placeOrder(resolveUser(principal).getId(), request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public List<OrderResponse> getOrderHistory(@AuthenticationPrincipal UserDetails principal) {
        return orderService.history(resolveUser(principal).getId());
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public OrderResponse cancelOrder(@PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetails principal) {
        return orderService.cancel(resolveUser(principal).getId(), orderId);
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public OrderResponse updateStatus(@PathVariable UUID orderId,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateStatusRequest request) {
        return orderService.updateStatus(resolveUser(principal).getId(), orderId, request);
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
