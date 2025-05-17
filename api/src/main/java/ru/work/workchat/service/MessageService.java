package ru.work.workchat.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.configuration.excepion.ChatNotFoundException;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.configuration.excepion.MessageNotFoundException;
import ru.work.workchat.model.dto.*;
import ru.work.workchat.model.entity.Chat;
import ru.work.workchat.model.entity.Message;
import ru.work.workchat.repository.ChatRepository;
import ru.work.workchat.repository.ChatUserRepository;
import ru.work.workchat.repository.MessageRepository;
import ru.work.workchat.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@AllArgsConstructor
@Service
public class MessageService {

    UserRepository userRepository;
    MessageRepository messageRepository;
    ChatRepository chatRepository;
    ChatUserRepository chatUserRepository;
    SimpMessagingTemplate messagingTemplate;

    private void sendMessageSentMessage(Long chatId, Message message){
        List<String> users = chatUserRepository.findByChatIdOrderByUser_Name(chatId).stream().map(
                (user) -> user.getUser().getUsername()).toList();

        for (String user : users){
            messagingTemplate.convertAndSendToUser(
                    user,
                    "/queue/messages",
                    new WebSocketMessageDTO("Message received", chatId, null, new MessageDTO(message))
            );
        }
    }

    public MessagesPageDTO getMessages(Long chatId, int page) {
        chatUserRepository.findByChatIdAndUser_Username(chatId,
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ChatNotFoundException("Чат не найден"));

        if (page < 0){
            throw new IllegalArgumentException("Неверный номер страницы");
        }

        return new MessagesPageDTO(messageRepository.findByChat_id(
                chatId,
                PageRequest.of(
                        page,
                        50,
                        Sort.by("createdAt").descending())));
    }

    public MessageDTO getLastMessage(Long chatId){
        chatUserRepository.findByChatIdAndUser_Username(chatId,
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ChatNotFoundException("Чат не найден"));

        Message message = messageRepository.findByChat_id(chatId,
                PageRequest.of(0, 1, Sort.by("createdAt").descending())).getContent()
                .stream().findFirst().orElseThrow(() -> new MessageNotFoundException("Сообщение не найдено"));

        return new MessageDTO(message);
    }

    public ImageFileDTO getMsgImage(Long msgId) {
        Message message = messageRepository.findById(msgId).orElseThrow(() -> new MessageNotFoundException("Сообщение не найдено"));

        chatUserRepository.findByChatIdAndUser_Username(
                message.getChat().getId(),
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new MessageNotFoundException("Сообщение не найдено"));

        byte[] imageData = message.getAttachmentFile();

        if (imageData == null || imageData.length == 0) {
            throw new ImageNotFoundException("Изображение не найдено");
        }

        return new ImageFileDTO(imageData,
                String.format("message_'%d'.jpg", message.getId()),
                "image/jpeg");
    }

    @Transactional
    public String sendMessage(Long chatId, StringDTO message) {
        Chat chat = chatUserRepository.findByChatIdAndUser_Username(chatId,
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ChatNotFoundException("Чат не найден")).getChat();

        if (message.getValue() == null || !message.doMatch("^.*\\S.*$", 1, 2000)) {
            throw new IllegalArgumentException("Неверный формат сообщения");
        }

        Message msg = new Message();
        msg.setText(message.getValue().strip());
        msg.setAttachment(false);
        msg.setChat(chat);
        msg.setSender(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get());

        msg = messageRepository.save(msg);

        chat.setLastMessageTime(msg.getCreatedAt());
        chatRepository.save(chat);

        Message finalMsg = msg;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendMessageSentMessage(chatId, finalMsg);
            }
        });

        return "Сообщение отправлено";
    }

    private boolean isJPEG(MultipartFile file) throws IOException {
        byte[] header = new byte[2];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
    }

    @Transactional
    public String sendImage(Long chatId, MultipartFile file) throws IOException {
        Chat chat = chatUserRepository.findByChatIdAndUser_Username(chatId,
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ChatNotFoundException("Чат не найден")).getChat();

        String originalFilename = file.getOriginalFilename();
        if (!isJPEG(file)
                || !file.getContentType().equals("image/jpeg")
                || (originalFilename != null
                && !originalFilename.toLowerCase().endsWith(".jpg")
                && !originalFilename.toLowerCase().endsWith(".jpeg"))) {
            throw new IllegalArgumentException("Неподдерживаемый формат");
        }

        Message msg = new Message();
        msg.setAttachmentFile(file.getBytes());
        msg.setAttachment(true);
        msg.setChat(chat);
        msg.setSender(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get());

        msg = messageRepository.save(msg);

        chat.setLastMessageTime(msg.getCreatedAt());
        chatRepository.save(chat);

        Message finalMsg = msg;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendMessageSentMessage(chatId, finalMsg);
            }
        });

        return "Сообщение отправлено";
    }
}
