package com.foodx.review;

import com.foodx.order.FoodOrder;
import com.foodx.restaurant.Restaurant;
import com.foodx.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    Optional<Review> findByOrder(FoodOrder order);

    List<Review> findByRestaurant(Restaurant restaurant);

    List<Review> findByUser(User user);
}
