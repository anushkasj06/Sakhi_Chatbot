package com.wms.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wms.dtos.request.CreateUserRequest;
import com.wms.dtos.request.UpdateUserRequest;
import com.wms.dtos.request.UpdateUserStatusRequest;
import com.wms.dtos.response.UserResponse;
import com.wms.exceptions.ApiException;
import com.wms.models.Role;
import com.wms.models.User;
import com.wms.models.UserRole;
import com.wms.repositories.RoleRepository;
import com.wms.repositories.UserRepository;
import com.wms.repositories.UserRoleRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setIsActive(Boolean.TRUE.equals(request.getActive()));
        user.setEmailVerified(false);
        user = userRepository.save(user);

        assignRoles(user, request.getRoles());
        return toResponse(user);
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateStatus(Long userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        user.setIsActive(request.getActive());
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserResponse assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Role role = roleRepository.findByRoleName(roleName.trim().toUpperCase())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found"));

        if (!userRoleRepository.existsByUserUserIdAndRoleRoleId(userId, role.getRoleId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }

        return toResponse(user);
    }

    @Transactional
    public UserResponse removeRole(Long userId, Integer roleId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        userRoleRepository.deleteByUserUserIdAndRoleRoleId(userId, roleId);
        return toResponse(user);
    }

    private void assignRoles(User user, List<String> roleNames) {
        List<Role> roles = roleNames.stream()
            .map(roleName -> roleRepository.findByRoleName(roleName.trim().toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found: " + roleName)))
            .toList();

        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(role);
            userRoleRepository.save(userRole);
        }
    }

    private UserResponse toResponse(User user) {
        List<UserRole> links = userRoleRepository.findByUser(user);
        List<String> roles = links.stream().map(link -> link.getRole().getRoleName()).toList();

        return new UserResponse(
            user.getUserId(),
            user.getName(),
            user.getEmail(),
            Boolean.TRUE.equals(user.getIsActive()),
            Boolean.TRUE.equals(user.getEmailVerified()),
            roles
        );
    }

    public Map<Long, List<String>> getRolesByUserIds(List<Long> userIds) {
        return userIds.stream().collect(Collectors.toMap(id -> id, id -> {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return List.of();
            }
            return userRoleRepository.findByUser(user).stream().map(ur -> ur.getRole().getRoleName()).toList();
        }));
    }
}
