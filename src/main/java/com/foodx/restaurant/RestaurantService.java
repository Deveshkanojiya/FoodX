package com.foodx.restaurant;

import com.foodx.config.CacheNames;
import com.foodx.common.RestaurantStatus;
import com.foodx.exception.ResourceNotFoundException;
import com.foodx.user.User;
import com.foodx.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RestaurantService {
    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public RestaurantResponse create(UUID ownerId, RestaurantRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        Restaurant restaurant = new Restaurant();
        restaurant.setOwner(owner);
        restaurant.setName(request.name());
        restaurant.setSlug(request.slug());
        restaurant.setDescription(request.description());
        restaurant.setCity(request.city());
        restaurant.setAddress(request.address());
        restaurant.setStatus(RestaurantStatus.CLOSED);
        restaurant.setOpenNow(false);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant created: {}", saved.getId());
        return toResponse(saved);
    }

    @Cacheable(cacheNames = CacheNames.RESTAURANT_DETAILS, key = "#id")
    @Transactional(readOnly = true)
    public RestaurantResponse get(UUID id) {
        return toResponse(findRestaurant(id));
    }

    @Cacheable(cacheNames = CacheNames.RESTAURANT_LIST, key = "#search")
    @Transactional(readOnly = true)
    public List<RestaurantResponse> search(String search) {
        List<Restaurant> restaurants = (search == null || search.isBlank())
                ? restaurantRepository.findAll()
                : restaurantRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(search, search);
        return restaurants.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> listByOwner(UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        return restaurantRepository.findByOwner(owner).stream().map(this::toResponse).toList();
    }

    @CacheEvict(cacheNames = { CacheNames.RESTAURANT_DETAILS, CacheNames.RESTAURANT_LIST, CacheNames.POPULAR_RESTAURANTS }, allEntries = true)
    public RestaurantResponse update(UUID id, UUID currentUserId, RestaurantRequest request) {
        Restaurant restaurant = findRestaurant(id);
        verifyOwnership(restaurant, currentUserId);
        restaurant.setName(request.name());
        restaurant.setSlug(request.slug());
        restaurant.setDescription(request.description());
        restaurant.setCity(request.city());
        restaurant.setAddress(request.address());
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant updated: restaurantId={}", saved.getId());
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = { CacheNames.RESTAURANT_DETAILS, CacheNames.RESTAURANT_LIST, CacheNames.POPULAR_RESTAURANTS }, allEntries = true)
    public RestaurantResponse open(UUID id, UUID currentUserId) {
        Restaurant restaurant = findRestaurant(id);
        verifyOwnership(restaurant, currentUserId);
        restaurant.setStatus(RestaurantStatus.OPEN);
        restaurant.setOpenNow(true);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant opened: restaurantId={}", saved.getId());
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = { CacheNames.RESTAURANT_DETAILS, CacheNames.RESTAURANT_LIST, CacheNames.POPULAR_RESTAURANTS }, allEntries = true)
    public RestaurantResponse close(UUID id, UUID currentUserId) {
        Restaurant restaurant = findRestaurant(id);
        verifyOwnership(restaurant, currentUserId);
        restaurant.setStatus(RestaurantStatus.CLOSED);
        restaurant.setOpenNow(false);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.info("Restaurant closed: restaurantId={}", saved.getId());
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = { CacheNames.RESTAURANT_DETAILS, CacheNames.RESTAURANT_LIST, CacheNames.POPULAR_RESTAURANTS }, allEntries = true)
    public RestaurantResponse disable(UUID id, UUID currentUserId) {
        Restaurant restaurant = findRestaurant(id);
        verifyOwnership(restaurant, currentUserId);
        restaurant.setStatus(RestaurantStatus.DISABLED);
        restaurant.setOpenNow(false);
        Restaurant saved = restaurantRepository.save(restaurant);
        log.warn("Restaurant disabled: restaurantId={}", saved.getId());
        return toResponse(saved);
    }

    public void verifyOwnership(Restaurant restaurant, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != com.foodx.common.UserRole.ADMIN && !restaurant.getOwner().getId().equals(user.getId())) {
            throw new com.foodx.exception.ForbiddenException("You do not have permission to modify this restaurant");
        }
    }

    public Restaurant findRestaurant(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getOwner().getId(),
                restaurant.getName(),
                restaurant.getSlug(),
                restaurant.getDescription(),
                restaurant.getCity(),
                restaurant.getAddress(),
                restaurant.getStatus(),
                restaurant.isOpenNow());
    }
}
