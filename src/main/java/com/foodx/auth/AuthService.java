package com.foodx.auth;

import com.foodx.common.AccountStatus;
import com.foodx.exception.BadRequestException;
import com.foodx.exception.UnauthorizedException;
import com.foodx.security.JwtService;
import com.foodx.user.User;
import com.foodx.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@Transactional
public class AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final com.foodx.config.JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            com.foodx.config.JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new BadRequestException("Email already registered");
        }

        if (request.role() == com.foodx.common.UserRole.ADMIN) {
            throw new BadRequestException("ADMIN role cannot be assigned through public registration");
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setRole(request.role() != null ? request.role() : com.foodx.common.UserRole.CUSTOMER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setEmailVerified(true);
        user = userRepository.save(user);

        log.info("User registered successfully: userId={}, email={}, role={}", user.getId(), user.getEmail(), user.getRole());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name(),
                accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Login rejected for non-active user: userId={}, status={}", user.getId(), user.getStatus());
            throw new UnauthorizedException("Account is not active");
        }

        log.info("User logged in successfully: userId={}, email={}", user.getId(), user.getEmail());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name(),
                accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (token.isRevoked() || token.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Token refresh rejected: revoked={}, expired={}", token.isRevoked(), token.getExpiresAt().isBefore(Instant.now()));
            throw new UnauthorizedException("Refresh token expired or revoked");
        }
        User user = token.getUser();
        log.info("Access token refreshed: userId={}", user.getId());
        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole().name(),
                accessToken, request.refreshToken());
    }

    public void logout(LogoutRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.info("User logged out successfully: userId={}", token.getUser().getId());
    }

    private String createRefreshToken(User user) {
        String raw = UUID.randomUUID() + ":" + user.getEmail() + ":" + Instant.now();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plusSeconds(60L * 60 * 24 * jwtProperties.refreshTokenTtlDays()));
        token.setRevoked(false);
        refreshTokenRepository.save(token);
        return raw;
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hashing algorithm not available", e);
        }
    }
}
