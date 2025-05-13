package ru.work.workchat.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.work.workchat.configuration.excepion.ChatNotFoundException;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.model.dto.ChatsPageDTO;
import ru.work.workchat.model.dto.ImageFileDTO;
import ru.work.workchat.model.dto.OperationResultDTO;
import ru.work.workchat.model.dto.UserChatsDTO;
import ru.work.workchat.model.entity.Chat;
import ru.work.workchat.model.entity.ChatUser;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.ChatRepository;
import ru.work.workchat.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.work.workchat.model.entity.ChatUser.Role.OWNER;

@AllArgsConstructor
@Service
public class ChatService {

    UserRepository userRepository;
    ChatRepository chatRepository;

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
    public OperationResultDTO createChat(String name){
        Chat chat = new Chat();
        chat.setName(name);
        chat.setLastMessageTime(LocalDateTime.now());

        chat = chatRepository.save(chat);
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        ChatUser creator = new ChatUser(chat, user, OWNER);
        chat.setChatUsers(new ArrayList<>(List.of(creator)));

        chatRepository.save(chat);

        return new OperationResultDTO("Чат создан", null);
    }
}
