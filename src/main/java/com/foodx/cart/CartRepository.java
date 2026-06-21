package com.foodx.cart;

import com.foodx.restaurant.Restaurant;
import com.foodx.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserAndActiveTrue(User user);

    Optional<Cart> findByUserAndRestaurantAndActiveTrue(User user, Restaurant restaurant);
}
