package com.foodx.admin;

import com.foodx.common.AccountStatus;
import com.foodx.common.RestaurantStatus;
import com.foodx.order.FoodOrderRepository;
import com.foodx.restaurant.RestaurantRepository;
import com.foodx.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AdminService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodOrderRepository orderRepository;

    public AdminService(UserRepository userRepository, RestaurantRepository restaurantRepository,
            FoodOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<UserView> getUsers() {
        return userRepository.findAll().stream().map(u -> new UserView(u.getId(), u.getEmail(),
                u.getFullName(), u.getRole().name(), u.getStatus())).toList();
    }

    public void suspendUser(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(AccountStatus.SUSPENDED);
            userRepository.save(user);
            log.warn("Admin suspended user: userId={}", userId);
        });
    }

    @Transactional(readOnly = true)
    public List<RestaurantView> getRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(r -> new RestaurantView(r.getId(), r.getName(), r.getCity(), r.getStatus())).toList();
    }

    @org.springframework.cache.annotation.CacheEvict(cacheNames = { com.foodx.config.CacheNames.RESTAURANT_DETAILS, com.foodx.config.CacheNames.RESTAURANT_LIST, com.foodx.config.CacheNames.POPULAR_RESTAURANTS }, allEntries = true)
    public void disableRestaurant(UUID restaurantId) {
        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            restaurant.setStatus(RestaurantStatus.DISABLED);
            restaurant.setOpenNow(false);
            restaurantRepository.save(restaurant);
            log.warn("Admin disabled restaurant: restaurantId={}", restaurantId);
        });
    }

    @Transactional(readOnly = true)
    public List<OrderView> getOrders() {
        return orderRepository.findAll().stream()
                .map(o -> new OrderView(o.getId(), o.getOrderNumber(), o.getStatus(), o.getTotalMinor()))
                .toList();
    }
}
