package com.whatsapp.whatsapp.repository;

import com.whatsapp.whatsapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);
    List<User> findByMobileNumberIn(List<String> mobileNumbers);
} 