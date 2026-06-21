package com.foodx.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FoodOrderItemRepository extends JpaRepository<FoodOrderItem, UUID> {
}
