package com.whatsapp.whatsapp.service.impl;

import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Override
    public User createUser(User user) {
        try {
            return userRepository.save(user);
        } catch (Exception e) {
            logger.error("Exception in UserServiceImpl", e);
            return null;
        }
    }

    @Override
    public Optional<User> getUserById(Long id) {
        try {
            return userRepository.findById(id);
        } catch (Exception e) {
            logger.error("Exception in UserServiceImpl", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Exception in UserServiceImpl", e);
            return Optional.empty();
        }
    }

    @Override
    public User updateUser(Long id, User update) {
        try {
            return userRepository.findById(id).map(user -> {
                user.setDisplayName(update.getDisplayName());
                user.setProfileUrl(update.getProfileUrl());
                return userRepository.save(user);
            }).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            logger.error("Exception in UserServiceImpl", e);
            return null;
        }
    }

    @Override
    public User updateProfileUrl(Long id, String profileUrl) {
        try {
            return userRepository.findById(id).map(user -> {
                user.setProfileUrl(profileUrl);
                return userRepository.save(user);
            }).orElseThrow(() -> new RuntimeException("User not found"));
        } catch (Exception e) {
            logger.error("Exception in UserServiceImpl", e);
            return null;
        }
    }

    @Override
    public Optional<User> getUserByCountryCodeAndMobileNumber(String countryCode, String mobileNumber) {
        try {
            return userRepository.findByCountryCodeAndMobileNumber(countryCode, mobileNumber);
        } catch (Exception e) {
            logger.error("Exception in UserServiceImpl", e);
            return Optional.empty();
        }
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

    @Override
    public List<User> getUsersByMobileNumbers(List<String> mobileNumbers) {
        try {
            return userRepository.findByMobileNumberIn(mobileNumbers);
        } catch (Exception e) {
            logger.error("Exception in UserServiceImpl", e);
            return List.of();
        }
    }
} 