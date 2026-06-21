package com.foodx.menu;

import com.foodx.common.MenuItemStatus;
import com.foodx.restaurant.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {
    List<MenuItem> findByRestaurant(Restaurant restaurant);

    List<MenuItem> findByRestaurantAndStatus(Restaurant restaurant, MenuItemStatus status);

    List<MenuItem> findByNameContainingIgnoreCase(String name);
}
