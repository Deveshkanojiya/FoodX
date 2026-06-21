package com.foodx.order;

import com.foodx.common.OrderStatus;
import com.foodx.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, UUID> {
    List<FoodOrder> findByUserOrderByCreatedAtDesc(User user);

    List<FoodOrder> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    Optional<FoodOrder> findByOrderNumber(String orderNumber);

    List<FoodOrder> findByStatus(OrderStatus status);
}
