package com.foodx.restaurant;

import com.foodx.user.User;
import com.foodx.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public RestaurantDtos.RestaurantResponse create(@AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RestaurantDtos.CreateRestaurantRequest request) {
        return restaurantService.create(resolveUser(principal).getId(), request);
    }

    @GetMapping("/{id}")
    public RestaurantDtos.RestaurantResponse get(@PathVariable UUID id) {
        return restaurantService.get(id);
    }

    @GetMapping
    public List<RestaurantDtos.RestaurantResponse> search(@RequestParam(required = false) String q) {
        return restaurantService.search(q);
    }

    @GetMapping("/owner/mine")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public List<RestaurantDtos.RestaurantResponse> mine(@AuthenticationPrincipal UserDetails principal) {
        return restaurantService.listByOwner(resolveUser(principal).getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public RestaurantDtos.RestaurantResponse update(@PathVariable UUID id,
            @Valid @RequestBody RestaurantDtos.UpdateRestaurantRequest request) {
        return restaurantService.update(id, request);
    }

    @PostMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public RestaurantDtos.RestaurantResponse open(@PathVariable UUID id) {
        return restaurantService.open(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public RestaurantDtos.RestaurantResponse close(@PathVariable UUID id) {
        return restaurantService.close(id);
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow();
    }
}
