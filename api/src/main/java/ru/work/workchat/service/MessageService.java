package ru.work.workchat.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.configuration.excepion.ChatNotFoundException;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.configuration.excepion.MessageNotFoundException;
import ru.work.workchat.model.dto.ImageFileDTO;
import ru.work.workchat.model.dto.MessageDTO;
import ru.work.workchat.model.dto.StringDTO;
import ru.work.workchat.model.entity.Chat;
import ru.work.workchat.model.entity.Message;
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
    ChatUserRepository chatUserRepository;

    public List<MessageDTO> getMessages(Long chatId, int page) {
        chatUserRepository.findByChatIdAndUser_Username(chatId,
                SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ChatNotFoundException("Чат не найден"));

        if (page < 0){
            throw new IllegalArgumentException("Неверный номер страницы");
        }

        return messageRepository.findByChat_id(
                chatId,
                PageRequest.of(
                        page,
                        100,
                        Sort.by("createdAt").descending())).stream().map(MessageDTO::new).toList();
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

    public String sendMessage(Long chatId, StringDTO message) {
        Chat chat = chatUserRepository.findByChatIdAndUser_Username(chatId,
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .orElseThrow(() -> new ChatNotFoundException("Чат не найден")).getChat();

        if (!message.doMatch("^.*\\S.*$", 1, 2000)) {
            throw new IllegalArgumentException("Неверный формат сообщения");
        }

        Message msg = new Message();
        msg.setText(message.getValue().strip());
        msg.setAttachment(false);
        msg.setChat(chat);
        msg.setSender(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get());

        messageRepository.save(msg);

        return "Сообщение отправлено";
    }

    private boolean isJPEG(MultipartFile file) throws IOException {
        byte[] header = new byte[2];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
    }

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

        messageRepository.save(msg);

        return "Сообщение отправлено";
    }
}
