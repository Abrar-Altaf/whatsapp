package com.whatsapp.whatsapp.service.impl;

import com.whatsapp.whatsapp.entity.Message;
import com.whatsapp.whatsapp.entity.MessageEmoji;
import com.whatsapp.whatsapp.entity.User;
import com.whatsapp.whatsapp.repository.MessageEmojiRepository;
import com.whatsapp.whatsapp.repository.MessageRepository;
import com.whatsapp.whatsapp.repository.UserRepository;
import com.whatsapp.whatsapp.service.EmojiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmojiServiceImpl implements EmojiService {
    private static final Logger logger = LoggerFactory.getLogger(EmojiServiceImpl.class);
    @Autowired
    private MessageEmojiRepository messageEmojiRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public MessageEmoji addOrReplaceEmoji(Long messageId, Long userId, String emojiTypeStr) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) throw new RuntimeException("User not found");
            Optional<Message> messageOpt = messageRepository.findById(messageId);
            if (messageOpt.isEmpty()) throw new RuntimeException("Message not found");
            MessageEmoji.EmojiType emojiType;
            try {
                emojiType = MessageEmoji.EmojiType.valueOf(emojiTypeStr.toUpperCase());
            } catch (Exception e) {
                throw new RuntimeException("Invalid emoji type");
            }
            if (!EnumSet.of(MessageEmoji.EmojiType.THUMBUP, MessageEmoji.EmojiType.LOVE, MessageEmoji.EmojiType.CRYING, MessageEmoji.EmojiType.SURPRISED).contains(emojiType)) {
                throw new RuntimeException("Unsupported emoji type");
            }
            messageEmojiRepository.findAll().stream()
                    .filter(e -> e.getMessage().getId().equals(messageId) && e.getUser().getId().equals(userId))
                    .findFirst()
                    .ifPresent(messageEmojiRepository::delete);
            // Add new reaction
            MessageEmoji emoji = MessageEmoji.builder()
                    .message(messageOpt.get())
                    .user(userOpt.get())
                    .emojiType(emojiType)
                    .build();
            return messageEmojiRepository.save(emoji);
        } catch (Exception e) {
            logger.error("Exception in EmojiServiceImpl", e);
            return null;
        }
    }

    @Override
    public List<MessageEmoji> getEmojisForMessage(Long messageId) {
        try {
            List<MessageEmoji> emojis = messageEmojiRepository.findAll();
            List<MessageEmoji> filtered = new ArrayList<>();
            for (MessageEmoji e : emojis) {
                if (e.getMessage().getId().equals(messageId)) {
                    filtered.add(e);
                }
            }
            return filtered;
        } catch (Exception e) {
            logger.error("Exception in EmojiServiceImpl", e);
            return new ArrayList<>();
        }
    }

    @Override
    public java.util.Optional<com.whatsapp.whatsapp.entity.User> getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            logger.error("Exception in EmojiServiceImpl", e);
            return Optional.empty();
        }
    }
} 