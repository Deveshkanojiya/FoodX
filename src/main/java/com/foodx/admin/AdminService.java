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
    public List<AdminDtos.UserView> users() {
        return userRepository.findAll().stream().map(u -> new AdminDtos.UserView(u.getId(), u.getEmail(),
                u.getFullName(), u.getRole().name(), u.getStatus())).toList();
    }

    public void suspendUser(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(AccountStatus.SUSPENDED);
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.RestaurantView> restaurants() {
        return restaurantRepository.findAll().stream()
                .map(r -> new AdminDtos.RestaurantView(r.getId(), r.getName(), r.getCity(), r.getStatus())).toList();
    }

    public void disableRestaurant(UUID restaurantId) {
        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            restaurant.setStatus(RestaurantStatus.DISABLED);
            restaurant.setOpenNow(false);
            restaurantRepository.save(restaurant);
        });
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.OrderView> orders() {
        return orderRepository.findAll().stream()
                .map(o -> new AdminDtos.OrderView(o.getId(), o.getOrderNumber(), o.getStatus(), o.getTotalMinor()))
                .toList();
    }
}
