package com.foodx.restaurant;

import com.foodx.user.User;
import com.foodx.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final UserRepository userRepository;

    public RestaurantController(RestaurantService restaurantService, UserRepository userRepository) {
        this.restaurantService = restaurantService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public RestaurantResponse create(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.create(resolveUser(principal).getId(), request);
    }

    @GetMapping("/{id}")
    public RestaurantResponse get(@PathVariable UUID id) {
        return restaurantService.get(id);
    }

    @GetMapping
    public List<RestaurantResponse> search(@RequestParam(required = false) String q) {
        return restaurantService.search(q);
    }

    @GetMapping("/owner/mine")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public List<RestaurantResponse> getMyRestaurants(@AuthenticationPrincipal UserDetails principal) {
        return restaurantService.listByOwner(resolveUser(principal).getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public RestaurantResponse update(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RestaurantRequest request) {
        return restaurantService.update(id, resolveUser(principal).getId(), request);
    }

    @PostMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public RestaurantResponse open(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return restaurantService.open(id, resolveUser(principal).getId());
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public RestaurantResponse close(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetails principal) {
        return restaurantService.close(id, resolveUser(principal).getId());
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow();
    }
}
