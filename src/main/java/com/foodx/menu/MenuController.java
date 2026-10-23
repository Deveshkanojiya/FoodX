package com.foodx.menu;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
public class MenuController {
    private final MenuService menuService;
    private final com.foodx.user.UserRepository userRepository;

    public MenuController(MenuService menuService, com.foodx.user.UserRepository userRepository) {
        this.menuService = menuService;
        this.userRepository = userRepository;
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public CategoryResponse createCategory(@PathVariable UUID restaurantId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal,
            @Valid @RequestBody CreateCategoryRequest request) {
        return menuService.createCategory(restaurantId, resolveUser(principal).getId(), request);
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ItemResponse createItem(@PathVariable UUID restaurantId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal,
            @Valid @RequestBody CreateItemRequest request) {
        return menuService.createItem(restaurantId, resolveUser(principal).getId(), request);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ItemResponse updateItem(@PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal,
            @Valid @RequestBody UpdateItemRequest request) {
        return menuService.updateItem(restaurantId, itemId, resolveUser(principal).getId(), request);
    }

    @PostMapping("/items/{itemId}/disable")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ItemResponse disableItem(@PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal) {
        return menuService.disableItem(restaurantId, itemId, resolveUser(principal).getId());
    }

    private com.foodx.user.User resolveUser(org.springframework.security.core.userdetails.UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }

    @GetMapping("/categories")
    public List<CategoryResponse> listCategories(@PathVariable UUID restaurantId) {
        return menuService.listCategories(restaurantId);
    }

    @GetMapping("/items")
    public List<ItemResponse> listItems(@PathVariable UUID restaurantId,
            @RequestParam(required = false) String q) {
        return menuService.listItems(restaurantId, q);
    }
}
