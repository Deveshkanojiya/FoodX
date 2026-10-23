package com.foodx.review;

import com.foodx.user.User;
import com.foodx.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserRepository userRepository;

    public ReviewController(ReviewService reviewService, UserRepository userRepository) {
        this.reviewService = reviewService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreateReviewRequest request) {
        return reviewService.create(resolveUser(principal).getId(), request);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("permitAll()")
    public List<ReviewResponse> getReviewsByRestaurant(@PathVariable UUID restaurantId) {
        return reviewService.restaurantReviews(restaurantId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN')")
    public List<ReviewResponse> getMyReviews(@AuthenticationPrincipal UserDetails principal) {
        return reviewService.myReviews(resolveUser(principal).getId());
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername()).orElseThrow();
    }
}
