package com.whatsapp.whatsapp.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
	
	@JsonProperty("country_code")
    private String countryCode;
	
	@JsonProperty("mobile_number")
    private String mobileNumber;
}
