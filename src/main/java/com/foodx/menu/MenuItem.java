package com.foodx.menu;

import com.foodx.common.BaseEntity;
import com.foodx.common.MenuItemStatus;
import com.foodx.restaurant.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "menu_items", indexes = {
        @Index(name = "idx_menu_items_restaurant_id", columnList = "restaurant_id"),
        @Index(name = "idx_menu_items_category_id", columnList = "category_id"),
        @Index(name = "idx_menu_items_status", columnList = "status")
})
public class MenuItem extends BaseEntity {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MenuCategory category;

    @Column(nullable = false, length = 140)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private long priceMinor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MenuItemStatus status = MenuItemStatus.ACTIVE;
}
