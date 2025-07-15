package com.whatsapp.whatsapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false,name = "display_name")
    @JsonProperty("display_name")
    private String displayName;

    @Column(name = "country_code", nullable = false)
    private String countryCode;

    @Column(name = "mobile_number", nullable = false)
    @JsonProperty("country_code")
    private String mobileNumber;

    @Column(name = "profile_url")
    @JsonProperty("profile_url")
    private String profileUrl;
} 