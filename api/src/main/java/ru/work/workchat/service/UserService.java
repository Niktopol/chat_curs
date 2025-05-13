package ru.work.workchat.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.configuration.excepion.UserNotFoundException;
import ru.work.workchat.model.dto.ImageFileDTO;
import ru.work.workchat.model.dto.ProfileEditDTO;
import ru.work.workchat.model.dto.UserInfoDTO;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.UserRepository;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
@Service
public class UserService {
    UserRepository userRepository;

    public UserInfoDTO profileByUsername(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return new UserInfoDTO(user.getName(), user.getUsername(), user.getIsOnline());
    }

    public UserInfoDTO profile(){
        return new UserInfoDTO(userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get());
    }

    @Transactional
    public String editProfile(ProfileEditDTO profile){
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        user.setName(profile.getName());
        userRepository.save(user);

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

        return "Фото профиля изменено";
    }

    @Transactional
    public String deleteProfilePicture() {
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).get();
        user.setProfilePic(null);
        userRepository.save(user);

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
