package ru.work.workchat.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.configuration.excepion.ChatNotFoundException;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.configuration.excepion.NoAuthorityException;
import ru.work.workchat.model.dto.*;
import ru.work.workchat.model.entity.Chat;
import ru.work.workchat.model.entity.ChatUser;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.ChatRepository;
import ru.work.workchat.repository.ChatUserRepository;
import ru.work.workchat.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.work.workchat.model.entity.ChatUser.Role.OWNER;

@AllArgsConstructor
@Service
public class ChatService {

    UserRepository userRepository;
    ChatRepository chatRepository;
    ChatUserRepository chatUserRepository;

    public UserChatsDTO getChats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return new UserChatsDTO(
                chatRepository.findByIsPrivateTrueAndChatUsers_User_UsernameOrderByLastMessageTimeDesc(username),
                chatRepository.findByIsPrivateFalseAndChatUsers_User_UsernameOrderByLastMessageTimeDesc(username),
                username
        );
    }

    public ChatsPageDTO getChatsByName(String name, int page){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Chat> chats = chatRepository.findByIsPrivateTrueAndChatUsers_User_Username(username);
        if (page < 0){
            throw new IllegalArgumentException("Неверный номер страницы");
        }
        if (!name.isEmpty() && !(name.startsWith("@") && name.length() == 1)) {
            Page<User> users;
            if (name.startsWith("@")){
                users = userRepository.findUsersByEmptyChatsAndUsername(
                        chats,
                        username,
                        name.substring(1),
                        PageRequest.of(page, 50, Sort.by("name").ascending())
                );
            } else {
                users = userRepository.findUsersByEmptyChatsAndName(
                        chats,
                        username,
                        name,
                        PageRequest.of(page, 50, Sort.by("name").ascending())
                );

            }
            return new ChatsPageDTO(users);
        } else {
            throw new IllegalArgumentException("Введите никнейм");
        }
    }

    public ImageFileDTO getChatPicture(Long id){
        byte[] imageData = chatRepository.findById(id).orElseThrow(() -> new ChatNotFoundException("Чат не найден")).getChatPic();

        if (imageData == null || imageData.length == 0) {
            throw new ImageNotFoundException("Изображение не найдено");
        }

        return new ImageFileDTO(imageData,
                String.format("chat_'%d'.jpg", id),
                "image/jpeg");
    }

    @Transactional
    public String createChat(StringDTO name){
        if (!name.doMatch("^.*\\S.*$", 1, 30)){
            throw new IllegalArgumentException("Неверное название чата");
        }
        Chat chat = new Chat();
        chat.setName(name.getValue());
        chat.setLastMessageTime(LocalDateTime.now());

        chat = chatRepository.save(chat);
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        ChatUser creator = new ChatUser(chat, user, OWNER);
        chat.setChatUsers(new ArrayList<>(List.of(creator)));

        chatRepository.save(chat);

        return "Чат создан";
    }

    private boolean isJPEG(MultipartFile file) throws IOException {
        byte[] header = new byte[2];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
    }

    private void checkChatEditAuthority(Long chatId){
        User owner = chatUserRepository.findByChatIdAndRole(chatId, OWNER).stream().findFirst().get().getUser();

        if (!Objects.equals(userRepository.findByUsername(
                        SecurityContextHolder.getContext().getAuthentication().getName())
                .get().getUsername(), owner.getUsername())) {
            throw new NoAuthorityException("Нет прав на изменение чата");
        }
    }

    @Transactional
    public String editChatPicture(MultipartFile file, Long chatId) throws IOException {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));
        checkChatEditAuthority(chatId);

        String originalFilename = file.getOriginalFilename();
        if (!isJPEG(file)
                || !file.getContentType().equals("image/jpeg")
                || (originalFilename != null
                && !originalFilename.toLowerCase().endsWith(".jpg")
                && !originalFilename.toLowerCase().endsWith(".jpeg"))) {
            throw new IllegalArgumentException("Неподдерживаемый формат фото");
        }

        chat.setChatPic(file.getBytes());
        chatRepository.save(chat);

        return "Фото чата изменено";
    }

    @Transactional
    public String editChatName(StringDTO name, Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));
        checkChatEditAuthority(chatId);

        if (!name.doMatch("^.*\\S.*$", 1, 30)){
            throw new IllegalArgumentException("Неверное название чата");
        }

        chat.setName(name.getValue());
        chatRepository.save(chat);

        return "Имя чата изменено";
    }

    @Transactional
    public String deleteChatPicture(Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));
        checkChatEditAuthority(chatId);

        chat.setChatPic(null);
        chatRepository.save(chat);

        return "Фото чата удалено";
    }
}

