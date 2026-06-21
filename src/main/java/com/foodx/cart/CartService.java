package com.foodx.cart;

import com.foodx.exception.BadRequestException;
import com.foodx.exception.ResourceNotFoundException;
import com.foodx.menu.MenuItem;
import com.foodx.menu.MenuItemRepository;
import com.foodx.restaurant.Restaurant;
import com.foodx.restaurant.RestaurantRepository;
import com.foodx.user.User;
import com.foodx.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public CartService(CartRepository cartRepository,
            UserRepository userRepository,
            RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public CartDtos.CartResponse getCart(UUID userId) {
        Cart cart = cartRepository.findByUserAndActiveTrue(findUser(userId)).orElseGet(() -> createEmptyCart(userId));
        return toResponse(cart);
    }

    public CartDtos.CartResponse addItem(UUID userId, CartDtos.AddItemRequest request) {
        User user = findUser(userId);
        Restaurant restaurant = findRestaurant(request.restaurantId());
        MenuItem item = findItem(request.menuItemId());
        Cart cart = cartRepository.findByUserAndActiveTrue(user).orElseGet(() -> createCart(user, restaurant));
        if (cart.getRestaurant() != null && !cart.getRestaurant().getId().equals(restaurant.getId())) {
            throw new BadRequestException("Cart can contain items from one restaurant only");
        }
        if (cart.getRestaurant() == null) {
            cart.setRestaurant(restaurant);
        }
        CartItem cartItem = cart.getItems().stream()
                .filter(existing -> existing.getMenuItem().getId().equals(item.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setMenuItem(item);
                    newItem.setQuantity(0);
                    cart.getItems().add(newItem);
                    return newItem;
                });
        cartItem.setQuantity(cartItem.getQuantity() + request.quantity());
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartDtos.CartResponse updateQuantity(UUID userId, UUID cartItemId, CartDtos.UpdateQuantityRequest request) {
        Cart cart = getActiveCart(userId);
        CartItem item = cart.getItems().stream().filter(ci -> ci.getId().equals(cartItemId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        item.setQuantity(request.quantity());
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartDtos.CartResponse removeItem(UUID userId, UUID cartItemId) {
        Cart cart = getActiveCart(userId);
        cart.getItems().removeIf(ci -> ci.getId().equals(cartItemId));
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartDtos.CartResponse clear(UUID userId) {
        Cart cart = getActiveCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        return toResponse(cart);
    }

    private Cart getActiveCart(UUID userId) {
        return cartRepository.findByUserAndActiveTrue(findUser(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private Cart createEmptyCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUser(findUser(userId));
        cart.setRestaurant(null);
        return cartRepository.save(cart);
    }

    private Cart createCart(User user, Restaurant restaurant) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setRestaurant(restaurant);
        return cartRepository.save(cart);
    }

    private User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Restaurant findRestaurant(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
    }

    private MenuItem findItem(UUID id) {
        return menuItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    private CartDtos.CartResponse toResponse(Cart cart) {
        List<CartDtos.CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartDtos.CartItemResponse(item.getId(), item.getMenuItem().getId(),
                        item.getMenuItem().getName(), item.getQuantity()))
                .toList();
        return new CartDtos.CartResponse(cart.getId(),
                cart.getRestaurant() == null ? null : cart.getRestaurant().getId(), cart.getUser().getId(), items);
    }
}
