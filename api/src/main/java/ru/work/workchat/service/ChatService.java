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
import ru.work.workchat.configuration.excepion.NoAuthorityException;
import ru.work.workchat.configuration.excepion.UserNotFoundException;
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
import java.util.Set;
import java.util.stream.Collectors;

import static ru.work.workchat.model.entity.ChatUser.Role.MEMBER;
import static ru.work.workchat.model.entity.ChatUser.Role.OWNER;

@AllArgsConstructor
@Service
public class ChatService {

    UserRepository userRepository;
    ChatRepository chatRepository;
    ChatUserRepository chatUserRepository;
    SimpMessagingTemplate messagingTemplate;

    private void sendChatUpdatedMessage(Long id, String extraUsername, String message){
        ArrayList<String> users = new ArrayList<>(chatUserRepository.findByChatIdOrderByUser_Name(id).stream().map(
                (user) -> user.getUser().getUsername()).toList());
        if (extraUsername != null){
            users.add(extraUsername);
        }
        for (String user : users){
            messagingTemplate.convertAndSendToUser(
                user,
                "/queue/messages",
                new WebSocketMessageDTO(message, id, null, null)
            );
        }
    }

    public UserChatsDTO getChats() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return new UserChatsDTO(
                chatRepository.findByIsPrivateTrueAndChatUsers_User_UsernameOrderByLastMessageTimeDesc(username),
                chatRepository.findByIsPrivateFalseAndChatUsers_User_UsernameOrderByLastMessageTimeDesc(username),
                username
        );
    }

    public ChatDTO getChatName(Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));

        if(chatUserRepository.findByChatIdAndUser_Username(
                chatId,
                SecurityContextHolder.getContext().getAuthentication().getName()).isEmpty()){
            throw new ChatNotFoundException("Чат не найден");
        }

        return new ChatDTO(
                chatId,
                null,
                chat.getName(),
                chat.isPrivate(),
                false);
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
        if(chatUserRepository.findByChatIdAndUser_Username(
                id,
                SecurityContextHolder.getContext().getAuthentication().getName()).isEmpty()){
            throw new ChatNotFoundException("Чат не найден");
        }

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

    @Transactional
    public ChatDTO createPrivateChat(String username){
        User user1 = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        User user2 = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Set<Long> idsFromList1 = chatRepository.findByIsPrivateTrueAndChatUsers_User_Username(user1.getUsername())
                .stream().map(Chat::getId)
                .collect(Collectors.toSet());

        boolean hasCommonId = chatRepository.findByIsPrivateTrueAndChatUsers_User_Username(user2.getUsername()).stream()
                .map(Chat::getId)
                .anyMatch(idsFromList1::contains);

        if (hasCommonId) {
            throw new IllegalArgumentException("Чат уже сществует");
        }

        Chat chat = new Chat();
        chat.setPrivate(true);
        chat.setLastMessageTime(LocalDateTime.now());

        ChatUser chatUser1 = new ChatUser(chat, user1, MEMBER);
        ChatUser chatUser2 = new ChatUser(chat, user2, MEMBER);

        chat.setChatUsers(new ArrayList<>(List.of(chatUser1, chatUser2)));

        Chat finalChat = chatRepository.save(chat);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendChatUpdatedMessage(finalChat.getId(), null, "Chat info updated");
            }
        });

        return new ChatDTO(finalChat.getId(), username, user2.getName(), true, false);
    }

    private boolean isJPEG(MultipartFile file) throws IOException {
        byte[] header = new byte[2];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
    }

    private void checkChatEditAuthority(Long chatId){
        User owner = chatUserRepository.findByChatIdAndRole(chatId, OWNER)
                .stream().findFirst().orElseThrow(() -> new ChatNotFoundException("Чат не найден")).getUser();

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
            throw new IllegalArgumentException("Неподдерживаемый формат");
        }

        chat.setChatPic(file.getBytes());
        chatRepository.save(chat);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendChatUpdatedMessage(chatId, null, "Chat image updated");
            }
        });

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

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendChatUpdatedMessage(chatId, null, "Chat info updated");
            }
        });

        return "Имя чата изменено";
    }

    @Transactional
    public String deleteChatPicture(Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));
        checkChatEditAuthority(chatId);

        chat.setChatPic(null);
        chatRepository.save(chat);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendChatUpdatedMessage(chatId, null, "Chat image updated");
            }
        });

        return "Фото чата удалено";
    }

    public List<UserInfoDTO> getChatUsers(Long chatId){
        if(chatUserRepository.findByChatIdAndUser_Username(
                chatId,
                SecurityContextHolder.getContext().getAuthentication().getName()).isEmpty()){
            throw new ChatNotFoundException("Чат не найден");
        }

        List<ChatUser> users = chatUserRepository.findByChatIdOrderByUser_Name(chatId);

        if (users.isEmpty()){
            throw new ChatNotFoundException("Чат не найден");
        }

        return users.stream().map((user) -> new UserInfoDTO(
                user.getUser().getId(),
                user.getUser().getName(),
                user.getUser().getUsername(),
                user.getRole() == OWNER,
                user.getUser().getIsOnline())).toList();
    }

    @Transactional
    public String addChatUser(StringDTO username, Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));
        checkChatEditAuthority(chatId);

        if (!username.doMatch("^[A-Za-z0-9\\-_]+$", 1, 20)){
            throw new IllegalArgumentException("Неверное имя пользователя");
        }

        if (chatUserRepository.findByChatIdAndUser_Username(chatId, username.getValue()).isPresent()){
            throw new IllegalArgumentException("Пользователь уже добавлен");
        }

        ChatUser user = new ChatUser(
                chat,
                userRepository.findByUsername(username.getValue()).orElseThrow(
                        () -> new UserNotFoundException("Пользователь не найден")
                ),
                MEMBER);

        chatUserRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendChatUpdatedMessage(chatId, username.getValue(), "Chat info updated");
            }
        });

        return "Пользователь добавлен";
    }

    @Transactional
    public String deleteChatUser(StringDTO username, Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));

        if (chat.isPrivate()){
            throw new ChatNotFoundException("Чат не найден");
        }

        if (!username.doMatch("^[A-Za-z0-9\\-_]+$", 1, 20)){
            throw new IllegalArgumentException("Неверное имя пользователя");
        }

        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(username.getValue())) {
            checkChatEditAuthority(chatId);
        }

        if (chatUserRepository.findByChatIdAndUser_Username(chatId, username.getValue()).isEmpty()){
            throw new IllegalArgumentException("Пользователь не является учатсником чата");
        }

        List<ChatUser> users = chatUserRepository.findByChatIdOrderByUser_Name(chatId);

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUser().getUsername().equals(username.getValue())){
                chatUserRepository.delete(users.get(i));
                users.remove(i);
                break;
            }
        }

        if (users.isEmpty()){
            chatRepository.delete(chat);
        } else {
            boolean hasOwner = false;
            for (ChatUser user : users) {
                if (user.getRole() == OWNER) {
                    hasOwner = true;
                    break;
                }
            }
            if (!hasOwner){
                users.get(0).setRole(OWNER);
                chatUserRepository.save(users.get(0));
            }
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendChatUpdatedMessage(chatId, username.getValue(), "Chat info updated");
            }
        });

        return "Участник чата удалён";
    }

    @Transactional
    public String setChatOwner(StringDTO username, Long chatId){
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Чат не найден"));
        checkChatEditAuthority(chatId);

        if (!username.doMatch("^[A-Za-z0-9\\-_]+$", 1, 20)){
            throw new IllegalArgumentException("Неверное имя пользователя");
        }

        ChatUser user = chatUserRepository.findByChatIdAndUser_Username(chatId, username.getValue())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не является учатсником чата"));

        ChatUser admin = chatUserRepository.findByChatIdAndUser_Username(
                chatId,
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).get();

        admin.setRole(MEMBER);
        user.setRole(OWNER);

        chatUserRepository.save(admin);
        chatUserRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendChatUpdatedMessage(chatId, username.getValue(), "Chat info updated");
            }
        });

        return "Владелец чата изменён";
    }
}

