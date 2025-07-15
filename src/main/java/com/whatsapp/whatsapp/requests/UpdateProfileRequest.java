package com.whatsapp.whatsapp.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {

	  @NotBlank(message = "Display name cannot be blank.")
	  @JsonProperty(value = "display_name")
      private String displayName;
}
