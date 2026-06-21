package com.foodx.menu;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public MenuDtos.CategoryResponse createCategory(@PathVariable UUID restaurantId,
            @Valid @RequestBody MenuDtos.CreateCategoryRequest request) {
        return menuService.createCategory(restaurantId, request);
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public MenuDtos.ItemResponse createItem(@PathVariable UUID restaurantId,
            @Valid @RequestBody MenuDtos.CreateItemRequest request) {
        return menuService.createItem(restaurantId, request);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public MenuDtos.ItemResponse updateItem(@PathVariable UUID restaurantId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MenuDtos.UpdateItemRequest request) {
        return menuService.updateItem(itemId, request);
    }

    @PostMapping("/items/{itemId}/disable")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public MenuDtos.ItemResponse disableItem(@PathVariable UUID restaurantId, @PathVariable UUID itemId) {
        return menuService.disableItem(itemId);
    }

    @GetMapping("/categories")
    public List<MenuDtos.CategoryResponse> categories(@PathVariable UUID restaurantId) {
        return menuService.listCategories(restaurantId);
    }

    @GetMapping("/items")
    public List<MenuDtos.ItemResponse> items(@PathVariable UUID restaurantId,
            @RequestParam(required = false) String q) {
        return menuService.listItems(restaurantId, q);
    }
}
