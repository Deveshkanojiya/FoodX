package com.foodx.restaurant;

import com.foodx.common.RestaurantStatus;
import com.foodx.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {
    List<Restaurant> findByOwner(User owner);

    List<Restaurant> findByStatus(RestaurantStatus status);

    Optional<Restaurant> findBySlug(String slug);

    List<Restaurant> findByNameContainingIgnoreCaseOrCityContainingIgnoreCase(String name, String city);
}
