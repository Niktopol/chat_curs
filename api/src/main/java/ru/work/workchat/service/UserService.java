package ru.work.workchat.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.configuration.excepion.UserNotFoundException;
import ru.work.workchat.model.dto.ImageFileDTO;
import ru.work.workchat.model.dto.StringDTO;
import ru.work.workchat.model.dto.UserInfoDTO;
import ru.work.workchat.model.dto.WebSocketMessageDTO;
import ru.work.workchat.model.entity.ChatUser;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.ChatRepository;
import ru.work.workchat.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Service
public class UserService {
    ChatRepository chatRepository;
    UserRepository userRepository;
    SimpMessagingTemplate messagingTemplate;

    private void sendUserUpdatedMessage(String username){
        List<String> users = chatRepository.findByChatUsers_User_Username(username)
                .stream().flatMap(chat -> chat.getChatUsers().stream())
                .map(chatUser -> chatUser.getUser().getUsername()).distinct().toList();

        for (String user : users){
            messagingTemplate.convertAndSendToUser(
                    user,
                    "/queue/messages",
                    new WebSocketMessageDTO("User info updated", null, username)
            );
        }
    }

    public UserInfoDTO profileByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return new UserInfoDTO(null, user.getName(), user.getUsername(), null, user.getIsOnline());
    }

    public UserInfoDTO profile(){
        return new UserInfoDTO(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get());
    }

    @Transactional
    public String editProfile(StringDTO name){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        if (!name.doMatch("^\\S+$", 1, 20)) {
            throw new IllegalArgumentException("Неверное имя пользователя");
        }
        user.setName(name.getValue());
        userRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendUserUpdatedMessage(user.getUsername());
            }
        });

        return "Данные профиля обновлены";
    }

    private boolean isJPEG(MultipartFile file) throws IOException {
        byte[] header = new byte[2];
        try (InputStream is = file.getInputStream()) {
            is.read(header);
        }

        return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
    }


    @Transactional
    public String editProfilePicture(MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();

        String originalFilename = file.getOriginalFilename();
        if (!isJPEG(file)
                || !file.getContentType().equals("image/jpeg")
                || (originalFilename != null
                    && !originalFilename.toLowerCase().endsWith(".jpg")
                    && !originalFilename.toLowerCase().endsWith(".jpeg"))) {
            throw new IllegalArgumentException("Неподдерживаемый формат фото");
        }

        user.setProfilePic(file.getBytes());
        userRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendUserUpdatedMessage(user.getUsername());
            }
        });

        return "Фото профиля изменено";
    }

    @Transactional
    public String deleteProfilePicture() {
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        user.setProfilePic(null);
        userRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendUserUpdatedMessage(user.getUsername());
            }
        });

        return "Фото профиля удалено";
    }

    public ImageFileDTO getProfilePicture(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        byte[] imageData = user.getProfilePic();

        if (imageData == null || imageData.length == 0) {
            throw new ImageNotFoundException("Изображение не найдено");
        }

        return new ImageFileDTO(imageData,
                String.format("profile_'%s'.jpg", username),
                "image/jpeg");
    }
}
