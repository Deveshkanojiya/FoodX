package com.foodx.order;

import com.foodx.cart.Cart;
import com.foodx.cart.CartItem;
import com.foodx.cart.CartRepository;
import com.foodx.common.OrderStatus;
import com.foodx.exception.BadRequestException;
import com.foodx.exception.ResourceNotFoundException;
import com.foodx.user.User;
import com.foodx.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final FoodOrderRepository orderRepository;
    private final FoodOrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public OrderService(FoodOrderRepository orderRepository, FoodOrderItemRepository orderItemRepository,
            CartRepository cartRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    public OrderResponse placeOrder(UUID userId, PlaceOrderRequest request) {
        User user = findUser(userId);
        Cart cart = cartRepository.findById(request.cartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cart does not belong to user");
        }
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        com.foodx.restaurant.Restaurant restaurant = cart.getRestaurant();
        if (restaurant == null || restaurant.getStatus() != com.foodx.common.RestaurantStatus.OPEN || !restaurant.isOpenNow()) {
            throw new BadRequestException("Restaurant is currently closed");
        }

        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getMenuItem().getStatus() != com.foodx.common.MenuItemStatus.ACTIVE) {
                throw new BadRequestException("Item " + cartItem.getMenuItem().getName() + " is currently unavailable");
            }
            if (!cartItem.getMenuItem().getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("Cart contains items from a different restaurant");
            }
        }

        FoodOrder order = new FoodOrder();
        order.setUser(user);
        order.setRestaurant(restaurant);
        order.setCart(cart);
        order.setOrderNumber("FX-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalMinor(
                cart.getItems().stream().mapToLong(i -> i.getMenuItem().getPriceMinor() * i.getQuantity()).sum());
        FoodOrder saved = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            FoodOrderItem item = new FoodOrderItem();
            item.setOrder(saved);
            item.setMenuItem(cartItem.getMenuItem());
            item.setItemName(cartItem.getMenuItem().getName());
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPriceMinor(cartItem.getMenuItem().getPriceMinor());
            item.setLineTotalMinor(cartItem.getMenuItem().getPriceMinor() * cartItem.getQuantity());
            saved.getItems().add(orderItemRepository.save(item));
        }
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Order placed: {}", saved.getOrderNumber());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> history(UUID userId) {
        User user = findUser(userId);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> restaurantOrders(UUID restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream().map(this::toResponse)
                .toList();
    }

    public OrderResponse updateStatus(UUID currentUserId, UUID orderId, UpdateStatusRequest request) {
        FoodOrder order = findOrder(orderId);
        User currentUser = findUser(currentUserId);
        if (currentUser.getRole() != com.foodx.common.UserRole.ADMIN
                && !order.getRestaurant().getOwner().getId().equals(currentUser.getId())) {
            throw new com.foodx.exception.ForbiddenException("Only the restaurant owner can update this order's status");
        }
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Order is already finalized");
        }
        order.setStatus(request.status());
        if (request.status() == OrderStatus.CANCELLED) {
            log.info("Order cancelled: {}", order.getOrderNumber());
        } else {
            log.info("Order updated: {} -> {}", order.getOrderNumber(), request.status());
        }
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse cancel(UUID currentUserId, UUID orderId) {
        FoodOrder order = findOrder(orderId);
        User currentUser = findUser(currentUserId);
        if (currentUser.getRole() != com.foodx.common.UserRole.ADMIN
                && !order.getUser().getId().equals(currentUser.getId())) {
            throw new com.foodx.exception.ForbiddenException("Cannot cancel another user's order");
        }
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Order is already finalized");
        }
        order.setStatus(OrderStatus.CANCELLED);
        log.info("Order cancelled: {}", order.getOrderNumber());
        return toResponse(orderRepository.save(order));
    }

    public FoodOrder findOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private OrderResponse toResponse(FoodOrder order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getId(),
                        item.getMenuItem() == null ? null : item.getMenuItem().getId(), item.getItemName(),
                        item.getQuantity(), item.getUnitPriceMinor(), item.getLineTotalMinor()))
                .toList();
        return new OrderResponse(order.getId(), order.getOrderNumber(), order.getUser().getId(),
                order.getRestaurant().getId(), order.getStatus(), order.getTotalMinor(), items);
    }
}
