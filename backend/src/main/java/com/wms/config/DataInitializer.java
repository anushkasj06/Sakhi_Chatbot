package com.wms.config;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.wms.enums.RoleName;
import com.wms.models.Role;
import com.wms.models.User;
import com.wms.models.UserRole;
import com.wms.repositories.RoleRepository;
import com.wms.repositories.UserRepository;
import com.wms.repositories.UserRoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdminProperties adminProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
        AdminProperties adminProperties,
        UserRepository userRepository,
        RoleRepository roleRepository,
        UserRoleRepository userRoleRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.adminProperties = adminProperties;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedRoles();
        seedSuperAdmin();
    }

    private void seedRoles() {
        List<String> defaultRoles = List.of(
            RoleName.ADMIN,
            RoleName.WAREHOUSE_MANAGER,
            RoleName.RECEIVING_CLERK,
            RoleName.PICKER,
            RoleName.PACKER,
            RoleName.CUSTOMER_SERVICE,
            RoleName.FINANCE,
            RoleName.AUDITOR,
            RoleName.CUSTOMER
        );

        for (String roleName : defaultRoles) {
            if (!roleRepository.existsByRoleName(roleName)) {
                Role role = new Role();
                role.setRoleName(roleName);
                roleRepository.save(role);
            }
        }
    }

    private void seedSuperAdmin() {
        String adminEmail = adminProperties.getEmail();
        String adminPassword = adminProperties.getPassword();

        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("Admin seed skipped because APP_ADMIN_EMAIL or APP_ADMIN_PASSWORD is missing.");
            return;
        }

        User admin = userRepository.findByEmailIgnoreCase(adminEmail).orElseGet(() -> {
            User user = new User();
            user.setName(adminProperties.getName() == null || adminProperties.getName().isBlank() ? "Super Admin" : adminProperties.getName());
            user.setEmail(adminEmail.toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
            user.setCreatedAt(LocalDateTime.now());
            user.setIsActive(true);
            user.setEmailVerified(true);
            return userRepository.save(user);
        });

        Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN).orElseThrow();

        if (!userRoleRepository.existsByUserUserIdAndRoleRoleId(admin.getUserId(), adminRole.getRoleId())) {
            UserRole userRole = new UserRole();
            userRole.setUser(admin);
            userRole.setRole(adminRole);
            userRoleRepository.save(userRole);
        }
    }
}
