package com.whatsapp.whatsapp.service;

import com.whatsapp.whatsapp.entity.User;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    User updateUser(Long id, User update);
} 