package com.whatsapp.whatsapp.requests;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateChatroomRequest {
	 private String name;
	 
	 @JsonProperty(value = "is_group")
     private Boolean isGroup;
	 
	 @JsonProperty(value = "member_ids")
     private List<Long> memberIds;
}
