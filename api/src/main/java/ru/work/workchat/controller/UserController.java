package ru.work.workchat.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.model.dto.ImageFileDTO;
import ru.work.workchat.model.dto.OperationResultDTO;
import ru.work.workchat.model.dto.ProfileEditDTO;
import ru.work.workchat.model.dto.UserInfoDTO;
import ru.work.workchat.service.UserService;

import java.io.IOException;

@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    UserService userService;

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserInfoDTO> profileByUsername(@PathVariable String username){
        return ResponseEntity.ok(userService.profileByUsername(username));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserInfoDTO> profile(){
        return ResponseEntity.ok(userService.profile());
    }

    @PatchMapping("/profile")
    public ResponseEntity<OperationResultDTO> editProfile(@RequestBody ProfileEditDTO profile){
        return ResponseEntity.ok(new OperationResultDTO(userService.editProfile(profile), null));
    }

    @GetMapping("/profilepic/{username}")
    public ResponseEntity<ByteArrayResource> getProfilePicture(@PathVariable String username){
        ImageFileDTO image = userService.getProfilePicture(username);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .contentLength(image.getData().length)
                .body(new ByteArrayResource(image.getData()));
    }

    @DeleteMapping("/profilepic")
    public ResponseEntity<OperationResultDTO> deleteProfilePicture(){
        return ResponseEntity.ok(new OperationResultDTO(userService.deleteProfilePicture(), null));
    }

    @PatchMapping("/profilepic")
    public ResponseEntity<OperationResultDTO> editProfilePicture(@RequestParam("file") MultipartFile file){
        try {
            return ResponseEntity.ok(new OperationResultDTO(userService.editProfilePicture(file), null));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OperationResultDTO(null, "Ошибка проверки изображения"));
        }
    }
}
