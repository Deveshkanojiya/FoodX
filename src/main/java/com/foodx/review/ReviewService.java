package com.foodx.review;

import com.foodx.common.OrderStatus;
import com.foodx.common.ReviewStatus;
import com.foodx.exception.BadRequestException;
import com.foodx.exception.ResourceNotFoundException;
import com.foodx.order.FoodOrder;
import com.foodx.order.FoodOrderRepository;
import com.foodx.restaurant.RestaurantRepository;
import com.foodx.user.User;
import com.foodx.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final FoodOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public ReviewService(ReviewRepository reviewRepository, FoodOrderRepository orderRepository,
            UserRepository userRepository, RestaurantRepository restaurantRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public ReviewResponse create(UUID userId, CreateReviewRequest request) {
        User user = findUser(userId);
        FoodOrder order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new com.foodx.exception.ForbiddenException("Cannot review another user's order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BadRequestException("Only delivered orders can be reviewed");
        }
        reviewRepository.findByOrder(order).ifPresent(existing -> {
            throw new BadRequestException("One review per order allowed");
        });
        Review review = new Review();
        review.setOrder(order);
        review.setUser(user);
        review.setRestaurant(order.getRestaurant());
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setStatus(ReviewStatus.PUBLISHED);
        Review saved = reviewRepository.save(review);
        log.info("Review created: reviewId={}, orderId={}, restaurantId={}, rating={}", saved.getId(), order.getId(), order.getRestaurant().getId(), request.rating());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> restaurantReviews(UUID restaurantId) {
        return reviewRepository.findByRestaurant(restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found")))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> myReviews(UUID userId) {
        return reviewRepository.findByUser(findUser(userId)).stream().map(this::toResponse).toList();
    }

    private User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(review.getId(), review.getOrder().getId(), review.getUser().getId(),
                review.getRestaurant().getId(), review.getRating(), review.getComment(), review.getStatus());
    }
}
