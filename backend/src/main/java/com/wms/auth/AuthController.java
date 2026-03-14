package com.wms.auth;

import com.wms.auth.dto.AuthResponse;
import com.wms.auth.dto.LoginRequest;
import com.wms.auth.dto.RegisterRequest;
import com.wms.user.Role;
import com.wms.user.User;
import com.wms.user.UserRepository;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);

    if (userRepository.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }

    String hash = passwordEncoder.encode(request.password());
    User user = new User(email, hash, Role.USER);
    userRepository.save(user);

    String token = jwtService.generateToken(user.getEmail(), user.getRole());
    return new AuthResponse(token);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    String email = request.email().trim().toLowerCase(Locale.ROOT);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    String token = jwtService.generateToken(user.getEmail(), user.getRole());
    return new AuthResponse(token);
  }
}
