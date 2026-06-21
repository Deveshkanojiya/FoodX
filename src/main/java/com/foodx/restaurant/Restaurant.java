package com.foodx.restaurant;

import com.foodx.common.BaseEntity;
import com.foodx.common.RestaurantStatus;
import com.foodx.user.User;
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
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurants_owner_id", columnList = "owner_id"),
        @Index(name = "idx_restaurants_status", columnList = "status"),
        @Index(name = "idx_restaurants_city", columnList = "city")
})
public class Restaurant extends BaseEntity {

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, unique = true, length = 140)
    private String name;

    @Column(nullable = false, length = 140)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RestaurantStatus status = RestaurantStatus.CLOSED;

    @Column(nullable = false)
    private boolean openNow = false;
}
