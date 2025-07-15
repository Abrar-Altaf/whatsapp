package com.whatsapp.whatsapp.service;

import com.whatsapp.whatsapp.entity.User;
import java.util.Optional;
import java.util.List;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    User updateUser(Long id, User update);
    User updateProfileUrl(Long id, String profileUrl);
    Optional<User> getUserByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);
    String generateUniqueUsername();
    List<User> getUsersByMobileNumbers(List<String> mobileNumbers);
} 