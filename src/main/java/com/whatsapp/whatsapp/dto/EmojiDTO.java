package com.whatsapp.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmojiDTO {
    @JsonProperty("user_id")
    public Long userId;
    @JsonProperty("emoji_type")
    public String emojiType;
    public EmojiDTO(Long userId, String emojiType) {
        this.userId = userId;
        this.emojiType = emojiType;
    }


}
