package com.foodx.restaurant;

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

    public RestaurantDtos.RestaurantResponse create(UUID ownerId, RestaurantDtos.CreateRestaurantRequest request) {
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

    @Cacheable(cacheNames = "restaurantDetails", key = "#id")
    @Transactional(readOnly = true)
    public RestaurantDtos.RestaurantResponse get(UUID id) {
        return toResponse(findRestaurant(id));
    }

    @Cacheable(cacheNames = "restaurantList", key = "#search")
    @Transactional(readOnly = true)
    public List<RestaurantDtos.RestaurantResponse> search(String search) {
        List<Restaurant> restaurants = (search == null || search.isBlank())
                ? restaurantRepository.findAll()
                : restaurantRepository.findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(search, search);
        return restaurants.stream().map(this::toResponse).toList();
    }

    public List<RestaurantDtos.RestaurantResponse> listByOwner(UUID ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        return restaurantRepository.findByOwner(owner).stream().map(this::toResponse).toList();
    }

    public RestaurantDtos.RestaurantResponse update(UUID id, RestaurantDtos.UpdateRestaurantRequest request) {
        Restaurant restaurant = findRestaurant(id);
        restaurant.setName(request.name());
        restaurant.setSlug(request.slug());
        restaurant.setDescription(request.description());
        restaurant.setCity(request.city());
        restaurant.setAddress(request.address());
        Restaurant saved = restaurantRepository.save(restaurant);
        return toResponse(saved);
    }

    @CacheEvict(cacheNames = { "restaurantDetails", "restaurantList", "popularRestaurants" }, allEntries = true)
    public RestaurantDtos.RestaurantResponse open(UUID id) {
        Restaurant restaurant = findRestaurant(id);
        restaurant.setStatus(RestaurantStatus.OPEN);
        restaurant.setOpenNow(true);
        return toResponse(restaurantRepository.save(restaurant));
    }

    @CacheEvict(cacheNames = { "restaurantDetails", "restaurantList", "popularRestaurants" }, allEntries = true)
    public RestaurantDtos.RestaurantResponse close(UUID id) {
        Restaurant restaurant = findRestaurant(id);
        restaurant.setStatus(RestaurantStatus.CLOSED);
        restaurant.setOpenNow(false);
        return toResponse(restaurantRepository.save(restaurant));
    }

    @CacheEvict(cacheNames = { "restaurantDetails", "restaurantList", "popularRestaurants" }, allEntries = true)
    public RestaurantDtos.RestaurantResponse disable(UUID id) {
        Restaurant restaurant = findRestaurant(id);
        restaurant.setStatus(RestaurantStatus.DISABLED);
        restaurant.setOpenNow(false);
        return toResponse(restaurantRepository.save(restaurant));
    }

    public Restaurant findRestaurant(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    private RestaurantDtos.RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantDtos.RestaurantResponse(
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
