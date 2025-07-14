package com.whatsapp.whatsapp.service.impl;

import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    // In-memory OTP store: key = countryCode+mobileNumber, value = otp
    private final ConcurrentHashMap<String, String> otpStore = new ConcurrentHashMap<>();
    private static final String DUMMY_OTP = "123456";

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User updateUser(Long id, User update) {
        return userRepository.findById(id).map(user -> {
            user.setDisplayName(update.getDisplayName());
            user.setProfileUrl(update.getProfileUrl());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateProfileUrl(Long id, String profileUrl) {
        return userRepository.findById(id).map(user -> {
            user.setProfileUrl(profileUrl);
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Optional<User> getUserByCountryCodeAndMobileNumber(String countryCode, String mobileNumber) {
        return userRepository.findByCountryCodeAndMobileNumber(countryCode, mobileNumber);
    }

    @Override
    public String generateUniqueUsername() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        String username;
        do {
            StringBuilder sb = new StringBuilder("username_");
            for (int i = 0; i < 6; i++) {
                sb.append(chars.charAt(random.nextInt(chars.length())));
            }
            username = sb.toString();
        } while (userRepository.findByUsername(username).isPresent());
        return username;
    }
} 