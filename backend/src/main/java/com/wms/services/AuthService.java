package com.wms.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CustomerSignupRequest;
import com.wms.dtos.request.ForgotPasswordRequest;
import com.wms.dtos.request.LoginRequest;
import com.wms.dtos.request.RefreshTokenRequest;
import com.wms.dtos.request.ResetPasswordRequest;
import com.wms.dtos.request.VerifyEmailRequest;
import com.wms.dtos.response.AuthResponse;
import com.wms.enums.RoleName;
import com.wms.exceptions.ApiException;
import com.wms.models.Customer;
import com.wms.models.EmailVerificationToken;
import com.wms.models.PasswordResetToken;
import com.wms.models.RefreshSession;
import com.wms.models.Role;
import com.wms.models.User;
import com.wms.models.UserRole;
import com.wms.repositories.CustomerRepository;
import com.wms.repositories.EmailVerificationTokenRepository;
import com.wms.repositories.PasswordResetTokenRepository;
import com.wms.repositories.RefreshSessionRepository;
import com.wms.repositories.RoleRepository;
import com.wms.repositories.UserRepository;
import com.wms.repositories.UserRoleRepository;
import com.wms.security.JwtTokenService;

import io.jsonwebtoken.Claims;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CustomerRepository customerRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthService(
        AuthenticationManager authenticationManager,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService,
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository,
        CustomerRepository customerRepository,
        RefreshSessionRepository refreshSessionRepository,
        EmailVerificationTokenRepository emailVerificationTokenRepository,
        PasswordResetTokenRepository passwordResetTokenRepository,
        EmailService emailService
    ) {
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.customerRepository = customerRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse customerSignup(CustomerSignupRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail()) || customerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(true);
        user.setEmailVerified(false);
        user = userRepository.save(user);

        Role customerRole = roleRepository.findByRoleName(RoleName.CUSTOMER)
            .orElseGet(() -> {
                Role role = new Role();
                role.setRoleName(RoleName.CUSTOMER);
                return roleRepository.save(role);
            });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(customerRole);
        userRoleRepository.save(userRole);

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setAddress(request.getAddress());
        customer.setPhoneNum(request.getPhoneNum());
        customer.setEmail(request.getEmail().toLowerCase());
        customer.setUser(user);
        customerRepository.save(customer);

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        verificationToken.setUsed(false);
        verificationToken.setCreatedAt(LocalDateTime.now());
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());

        return login(new LoginRequestBuilder(user.getEmail(), request.getPassword()).build());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        List<String> roles = userRoleRepository.findByUser(user).stream()
            .map(ur -> ur.getRole().getRoleName())
            .toList();

        String accessToken = jwtTokenService.generateAccessToken(user.getUserId(), user.getEmail(), roles);

        String refreshTokenId = UUID.randomUUID().toString();
        String refreshToken = jwtTokenService.generateRefreshToken(user.getUserId(), user.getEmail(), refreshTokenId);

        RefreshSession session = new RefreshSession();
        session.setUser(user);
        session.setTokenId(refreshTokenId);
        session.setRefreshTokenHash(hash(refreshToken));
        session.setExpiresAt(LocalDateTime.now().plusMinutes(10080));
        session.setRevoked(false);
        session.setCreatedAt(LocalDateTime.now());
        refreshSessionRepository.save(session);

        return new AuthResponse(accessToken, refreshToken, "Bearer", jwtTokenService.getAccessTokenTtlSeconds());
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        Claims claims;
        try {
            claims = jwtTokenService.parseToken(request.getRefreshToken());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        if (!jwtTokenService.isRefreshToken(claims)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token is not a refresh token");
        }

        String tokenId = claims.getId();
        RefreshSession session = refreshSessionRepository.findByTokenId(tokenId)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh session not found"));

        if (Boolean.TRUE.equals(session.getRevoked()) || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh session is expired or revoked");
        }

        if (!session.getRefreshTokenHash().equals(hash(request.getRefreshToken()))) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token mismatch");
        }

        User user = session.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User account is inactive");
        }

        List<String> roles = userRoleRepository.findByUser(user).stream()
            .map(ur -> ur.getRole().getRoleName())
            .toList();

        session.setRevoked(true);
        refreshSessionRepository.save(session);

        String accessToken = jwtTokenService.generateAccessToken(user.getUserId(), user.getEmail(), roles);

        String newTokenId = UUID.randomUUID().toString();
        String newRefreshToken = jwtTokenService.generateRefreshToken(user.getUserId(), user.getEmail(), newTokenId);

        RefreshSession newSession = new RefreshSession();
        newSession.setUser(user);
        newSession.setTokenId(newTokenId);
        newSession.setRefreshTokenHash(hash(newRefreshToken));
        newSession.setExpiresAt(LocalDateTime.now().plusMinutes(10080));
        newSession.setRevoked(false);
        newSession.setCreatedAt(LocalDateTime.now());
        refreshSessionRepository.save(newSession);

        return new AuthResponse(accessToken, newRefreshToken, "Bearer", jwtTokenService.getAccessTokenTtlSeconds());
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        Claims claims;
        try {
            claims = jwtTokenService.parseToken(request.getRefreshToken());
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        if (!jwtTokenService.isRefreshToken(claims)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token is not a refresh token");
        }

        String tokenId = claims.getId();
        refreshSessionRepository.findByTokenId(tokenId).ifPresent(session -> {
            session.setRevoked(true);
            refreshSessionRepository.save(session);
        });
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (Boolean.TRUE.equals(token.getUsed()) || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Verification token expired or already used");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmailIgnoreCase(request.getEmail()).ifPresent(user -> {
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken(UUID.randomUUID().toString());
            token.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            token.setUsed(false);
            token.setCreatedAt(LocalDateTime.now());
            passwordResetTokenRepository.save(token);

            emailService.sendPasswordResetEmail(user.getEmail(), token.getToken());
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid reset token"));

        if (Boolean.TRUE.equals(token.getUsed()) || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token expired or already used");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to hash token");
        }
    }

    private static final class LoginRequestBuilder {
        private final String email;
        private final String password;

        private LoginRequestBuilder(String email, String password) {
            this.email = email;
            this.password = password;
        }

        private LoginRequest build() {
            LoginRequest request = new LoginRequest();
            request.setEmail(email);
            request.setPassword(password);
            return request;
        }
    }
}
