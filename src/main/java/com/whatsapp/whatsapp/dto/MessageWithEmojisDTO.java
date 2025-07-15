package com.whatsapp.whatsapp.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.whatsapp.whatsapp.entity.Message;

public class MessageWithEmojisDTO {

    @JsonProperty("id")
    public Long id;
    
    @JsonProperty("content")
    public String content;
    
	@JsonProperty("attachment_url")
    public String attachmentUrl;
	
	@JsonProperty("attachment_type")
    public String attachmentType;
	
	@JsonProperty("sender_id")
    public Long senderId;
	
	@JsonProperty("chatroom_id")
    public Long chatroomId;
	
	@JsonProperty("created_at")
    public String createdAt;
	
    @JsonProperty("emojis")
    public List<EmojiDTO> emojis;
    public MessageWithEmojisDTO(Message msg, List<EmojiDTO> emojis) {
        this.id = msg.getId();
        this.content = msg.getContent();
        this.attachmentUrl = msg.getAttachmentUrl();
        this.attachmentType = msg.getAttachmentType() != null ? msg.getAttachmentType().name() : null;
        this.senderId = msg.getSender() != null ? msg.getSender().getId() : null;
        this.chatroomId = msg.getChatroom() != null ? msg.getChatroom().getId() : null;
        this.createdAt = msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null;
        this.emojis = emojis;
    }


}
