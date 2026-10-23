package com.foodx.cart;

import com.foodx.user.User;
import com.foodx.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public CartResponse getCart(@AuthenticationPrincipal UserDetails principal) {
        return cartService.getCart(resolveUser(principal).getId());
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public CartResponse addItem(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody AddItemRequest request) {
        return cartService.addItem(resolveUser(principal).getId(), request);
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public CartResponse updateQuantity(@AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID cartItemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        return cartService.updateQuantity(resolveUser(principal).getId(), cartItemId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public CartResponse removeItem(@AuthenticationPrincipal UserDetails principal, @PathVariable UUID cartItemId) {
        return cartService.removeItem(resolveUser(principal).getId(), cartItemId);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public CartResponse clearCart(@AuthenticationPrincipal UserDetails principal) {
        return cartService.clear(resolveUser(principal).getId());
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
