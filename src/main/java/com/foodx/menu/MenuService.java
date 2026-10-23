package com.foodx.menu;

import com.foodx.config.CacheNames;
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
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MenuService.class);

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;
    private final com.foodx.user.UserRepository userRepository;

    public MenuService(MenuCategoryRepository categoryRepository, MenuItemRepository itemRepository,
            RestaurantRepository restaurantRepository, com.foodx.user.UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    @CacheEvict(cacheNames = { CacheNames.MENU_BY_RESTAURANT, CacheNames.MENU_ITEMS_BY_RESTAURANT }, allEntries = true)
    public CategoryResponse createCategory(UUID restaurantId, UUID currentUserId, CreateCategoryRequest request) {
        Restaurant restaurant = findRestaurant(restaurantId);
        verifyOwnership(restaurant, currentUserId);
        MenuCategory category = new MenuCategory();
        category.setRestaurant(restaurant);
        category.setName(request.name());
        category.setSlug(request.slug());
        MenuCategory saved = categoryRepository.save(category);
        log.info("Menu category created: categoryId={}, restaurantId={}", saved.getId(), restaurantId);
        return toCategoryResponse(saved);
    }

    @CacheEvict(cacheNames = { CacheNames.MENU_BY_RESTAURANT, CacheNames.MENU_ITEMS_BY_RESTAURANT }, allEntries = true)
    public ItemResponse createItem(UUID restaurantId, UUID currentUserId, CreateItemRequest request) {
        Restaurant restaurant = findRestaurant(restaurantId);
        verifyOwnership(restaurant, currentUserId);
        MenuCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getRestaurant().getId().equals(restaurant.getId())) {
            throw new com.foodx.exception.BadRequestException("Category does not belong to this restaurant");
        }
        MenuItem item = new MenuItem();
        item.setRestaurant(restaurant);
        item.setCategory(category);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPriceMinor(request.priceMinor());
        item.setStatus(MenuItemStatus.ACTIVE);
        MenuItem saved = itemRepository.save(item);
        log.info("Menu item created: itemId={}, restaurantId={}", saved.getId(), restaurantId);
        return toItemResponse(saved);
    }

    @CacheEvict(cacheNames = { CacheNames.MENU_BY_RESTAURANT, CacheNames.MENU_ITEMS_BY_RESTAURANT }, allEntries = true)
    public ItemResponse updateItem(UUID restaurantId, UUID itemId, UUID currentUserId, UpdateItemRequest request) {
        Restaurant restaurant = findRestaurant(restaurantId);
        verifyOwnership(restaurant, currentUserId);
        MenuItem item = findItem(itemId);
        if (!item.getRestaurant().getId().equals(restaurant.getId())) {
            throw new com.foodx.exception.BadRequestException("Item does not belong to this restaurant");
        }
        MenuCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.getRestaurant().getId().equals(restaurant.getId())) {
            throw new com.foodx.exception.BadRequestException("Category does not belong to this restaurant");
        }
        item.setCategory(category);
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPriceMinor(request.priceMinor());
        item.setStatus(request.status());
        MenuItem saved = itemRepository.save(item);
        log.info("Menu item updated: itemId={}, restaurantId={}", saved.getId(), restaurantId);
        return toItemResponse(saved);
    }

    @CacheEvict(cacheNames = { CacheNames.MENU_BY_RESTAURANT, CacheNames.MENU_ITEMS_BY_RESTAURANT }, allEntries = true)
    public ItemResponse disableItem(UUID restaurantId, UUID itemId, UUID currentUserId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        verifyOwnership(restaurant, currentUserId);
        MenuItem item = findItem(itemId);
        if (!item.getRestaurant().getId().equals(restaurant.getId())) {
            throw new com.foodx.exception.BadRequestException("Item does not belong to this restaurant");
        }
        item.setStatus(MenuItemStatus.INACTIVE);
        MenuItem saved = itemRepository.save(item);
        log.info("Menu item disabled: itemId={}, restaurantId={}", saved.getId(), restaurantId);
        return toItemResponse(saved);
    }

    private void verifyOwnership(Restaurant restaurant, UUID userId) {
        com.foodx.user.User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != com.foodx.common.UserRole.ADMIN && !restaurant.getOwner().getId().equals(user.getId())) {
            throw new com.foodx.exception.ForbiddenException("You do not have permission to modify this restaurant's menu");
        }
    }

    @Cacheable(cacheNames = CacheNames.MENU_BY_RESTAURANT, key = "#restaurantId")
    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(UUID restaurantId) {
        Restaurant restaurant = findRestaurant(restaurantId);
        return categoryRepository.findByRestaurant(restaurant).stream().map(this::toCategoryResponse).toList();
    }

    @Cacheable(cacheNames = CacheNames.MENU_ITEMS_BY_RESTAURANT, key = "#restaurantId + ':' + #q")
    @Transactional(readOnly = true)
    public List<ItemResponse> listItems(UUID restaurantId, String q) {
        Restaurant restaurant = findRestaurant(restaurantId);
        List<MenuItem> items = itemRepository.findByRestaurant(restaurant);
        if (q != null && !q.isBlank()) {
            items = items.stream().filter(item -> item.getName().toLowerCase().contains(q.toLowerCase())).toList();
        }
        return items.stream().map(this::toItemResponse).toList();
    }

    public Restaurant findRestaurant(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    public MenuItem findItem(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    private CategoryResponse toCategoryResponse(MenuCategory category) {
        return new CategoryResponse(category.getId(), category.getRestaurant().getId(), category.getName(),
                category.getSlug(), category.isActive());
    }

    private ItemResponse toItemResponse(MenuItem item) {
        return new ItemResponse(item.getId(), item.getRestaurant().getId(),
                item.getCategory() == null ? null : item.getCategory().getId(), item.getName(), item.getDescription(),
                item.getPriceMinor(), item.getStatus());
    }
}
