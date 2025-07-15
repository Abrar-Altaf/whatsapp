package com.whatsapp.whatsapp.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.whatsapp.whatsapp.entity.Message;

public class MessageWithEmojisDTO {

    public Long id;
    
    public String content;
    
	@JsonProperty(value = "attachment_url")
    public String attachmentUrl;
	
	@JsonProperty(value = "attachment_type")
    public String attachmentType;
	
	@JsonProperty(value = "sender_id")
    public Long senderId;
	
	@JsonProperty(value = "chatroom_id")
    public Long chatroomId;
	
	@JsonProperty(value = "created_at")
    public String createdAt;
	
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
