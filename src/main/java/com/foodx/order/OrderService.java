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

    public OrderDtos.OrderResponse placeOrder(UUID userId, OrderDtos.PlaceOrderRequest request) {
        User user = findUser(userId);
        Cart cart = cartRepository.findById(request.cartId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Cart does not belong to user");
        }
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        FoodOrder order = new FoodOrder();
        order.setUser(user);
        order.setRestaurant(cart.getRestaurant());
        order.setCart(cart);
        order.setOrderNumber("FX-" + System.currentTimeMillis());
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
    public List<OrderDtos.OrderResponse> history(UUID userId) {
        User user = findUser(userId);
        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> restaurantOrders(UUID restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream().map(this::toResponse)
                .toList();
    }

    public OrderDtos.OrderResponse updateStatus(UUID orderId, OrderDtos.UpdateStatusRequest request) {
        FoodOrder order = findOrder(orderId);
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

    public OrderDtos.OrderResponse cancel(UUID orderId) {
        return updateStatus(orderId, new OrderDtos.UpdateStatusRequest(OrderStatus.CANCELLED));
    }

    public FoodOrder findOrder(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private OrderDtos.OrderResponse toResponse(FoodOrder order) {
        List<OrderDtos.OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderDtos.OrderItemResponse(item.getId(),
                        item.getMenuItem() == null ? null : item.getMenuItem().getId(), item.getItemName(),
                        item.getQuantity(), item.getUnitPriceMinor(), item.getLineTotalMinor()))
                .toList();
        return new OrderDtos.OrderResponse(order.getId(), order.getOrderNumber(), order.getUser().getId(),
                order.getRestaurant().getId(), order.getStatus(), order.getTotalMinor(), items);
    }
}
