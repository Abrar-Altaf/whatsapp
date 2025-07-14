package com.whatsapp.whatsapp.service;

import com.whatsapp.whatsapp.entity.MessageEmoji;
import java.util.List;

public interface EmojiService {
    MessageEmoji addOrReplaceEmoji(Long messageId, Long userId, String emojiType);
    List<MessageEmoji> getEmojisForMessage(Long messageId);
    java.util.Optional<com.whatsapp.whatsapp.entity.User> getUserByUsername(String username);
} 