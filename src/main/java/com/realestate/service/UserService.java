package com.realestate.service;

import com.realestate.model.Role;
import com.realestate.model.User;
import com.realestate.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public UserService(UserRepository userRepository,
                       @Lazy PasswordEncoder passwordEncoder,
                       ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    public long countAllUsers() {
        return userRepository.count();
    }

    public User registerUser(String name, String email, String password, Role role, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered: " + email);
        }
        User user = new User(name, email, passwordEncoder.encode(password), role, phone);
        User saved = userRepository.save(user);
        activityLogService.logActivity(saved, "USER_REGISTERED", "Registered with role: " + role.getDisplayName());
        return saved;
    }

    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already in use: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepository.save(user);
        activityLogService.logActivity(saved, "USER_CREATED", "Admin created account for " + saved.getEmail());
        return saved;
    }

    public User updateUser(Long id, String name, String email, Role role, String phone, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Check if email changed and if new email already taken
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already taken: " + email);
        }

        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setPhoneNumber(phone);
        if (enabled != null) {
            user.setEnabled(enabled);
        }

        User updated = userRepository.save(user);
        activityLogService.logActivity(updated, "USER_UPDATED", "Account updated for " + updated.getEmail());
        return updated;
    }

    public User updateProfile(User user, String name, String phone, String bio, String rentalHistory) {
        user.setName(name);
        user.setPhoneNumber(phone);
        user.setBio(bio);
        user.setRentalHistoryNotes(rentalHistory);
        User saved = userRepository.save(user);
        activityLogService.logActivity(saved, "PROFILE_UPDATED", "Updated personal profile information");
        return saved;
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        activityLogService.logActivity(user, "PASSWORD_CHANGED", "User updated account password");
    }

    public void deleteUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            activityLogService.logActivity(null, "USER_DELETED", "Deleted user " + user.getEmail() + " (" + user.getRole() + ")");
            userRepository.delete(user);
        });
    }
}
