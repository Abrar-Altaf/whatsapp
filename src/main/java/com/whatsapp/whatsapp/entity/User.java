package com.whatsapp.whatsapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"country_code", "mobile_number"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String displayName;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "mobile_number", nullable = false)
    private String mobileNumber;

    private String profileUrl;
} 