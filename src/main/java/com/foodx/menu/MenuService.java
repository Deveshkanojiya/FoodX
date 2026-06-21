package com.foodx.menu;

import com.foodx.common.MenuItemStatus;
import com.foodx.exception.ResourceNotFoundException;
import com.foodx.restaurant.Restaurant;
import com.foodx.restaurant.RestaurantRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MenuService {
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuService(MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository,
            RestaurantRepository restaurantRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public MenuDtos.CategoryResponse createCategory(UUID restaurantId, MenuDtos.CreateCategoryRequest request) {
        Restaurant restaurant = findRestaurant(restaurantId);
        MenuCategory category = new MenuCategory();
        category.setRestaurant(restaurant);
        category.setName(request.name());
        category.setSlug(request.slug());
        MenuCategory saved = categoryRepository.save(category);
        return toCategoryResponse(saved);
    }

    public MenuDtos.ItemResponse createItem(UUID restaurantId, MenuDtos.CreateItemRequest request) {
        Restaurant restaurant = findRestaurant(restaurantId);
        MenuCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setCategory(category);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPriceMinor(request.priceMinor());
        item.setStatus(MenuItemStatus.ACTIVE);
        return toItemResponse(itemRepository.save(item));
    }

    public MenuDtos.ItemResponse updateItem(UUID itemId, MenuDtos.UpdateItemRequest request) {
        MenuItem item = findItem(itemId);
        MenuCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        item.setCategory(category);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPriceMinor(request.priceMinor());
        item.setStatus(request.status());
        return toItemResponse(itemRepository.save(item));
    }

    public MenuDtos.ItemResponse disableItem(UUID itemId) {
        MenuItem item = findItem(itemId);
        item.setStatus(MenuItemStatus.INACTIVE);
        return toItemResponse(itemRepository.save(item));
    }

    @Cacheable(cacheNames = "menuByRestaurant", key = "#restaurantId")
    @Transactional(readOnly = true)
    public List<MenuDtos.CategoryResponse> listCategories(UUID restaurantId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        return categoryRepository.findByRestaurant(restaurant).stream().map(this::toCategoryResponse).toList();
    }

    @Cacheable(cacheNames = "menuItemsByRestaurant", key = "#restaurantId + ':' + #q")
    @Transactional(readOnly = true)
    public List<MenuDtos.ItemResponse> listItems(UUID restaurantId, String q) {
        Restaurant restaurant = findRestaurant(restaurantId);
        List<MenuItem> items = itemRepository.findByRestaurant(restaurant);
        if (q != null && !q.isBlank()) {
            items = items.stream().filter(item -> item.getName().toLowerCase().contains(q.toLowerCase())).toList();
        }
        return items.stream().map(this::toItemResponse).toList();
    }

    @CacheEvict(cacheNames = { "menuByRestaurant", "menuItemsByRestaurant" }, allEntries = true)
    public void evictRestaurantMenuCache(UUID restaurantId) {
        // Intentional no-op: the annotation performs cache eviction for the restaurant
        // menu caches.
    }

    public Restaurant findRestaurant(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    public MenuItem findItem(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    private MenuDtos.CategoryResponse toCategoryResponse(MenuCategory category) {
        return new MenuDtos.CategoryResponse(category.getId(), category.getRestaurant().getId(), category.getName(),
                category.getSlug(), category.isActive());
    }

    private MenuDtos.ItemResponse toItemResponse(MenuItem item) {
        return new MenuDtos.ItemResponse(item.getId(), item.getRestaurant().getId(),
                item.getCategory() == null ? null : item.getCategory().getId(), item.getName(), item.getDescription(),
                item.getPriceMinor(), item.getStatus());
    }
}
