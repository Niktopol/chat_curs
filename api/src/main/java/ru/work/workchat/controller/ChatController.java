package ru.work.workchat.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.model.dto.*;
import ru.work.workchat.service.ChatService;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/chats")
public class ChatController {

    ChatService chatService;

    @GetMapping
    public ResponseEntity<?> getChats(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "page", defaultValue = "0") int page){
        if (name.isEmpty()){
            return ResponseEntity.ok(chatService.getChats());
        } else {
            return ResponseEntity.ok(chatService.getChatsByName(name, page));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatDTO> getChatName(@PathVariable Long id){
        return ResponseEntity.ok(chatService.getChatName(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OperationResultDTO> editChatName(@RequestBody StringDTO name, @PathVariable Long id){
        return ResponseEntity.ok(new OperationResultDTO(chatService.editChatName(name, id), null));
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<ByteArrayResource> getChatPicture(@PathVariable Long id){
        ImageFileDTO image = chatService.getChatPicture(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .contentLength(image.getData().length)
                .body(new ByteArrayResource(image.getData()));
    }

    @PatchMapping("/image/{id}")
    public ResponseEntity<OperationResultDTO> editChatPicture(@RequestParam("file") MultipartFile file, @PathVariable Long id){
        try {
            return ResponseEntity.ok(new OperationResultDTO(chatService.editChatPicture(file, id), null));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OperationResultDTO(null, "Ошибка проверки изображения"));
        }
    }

    @DeleteMapping("/image/{id}")
    public ResponseEntity<OperationResultDTO> deleteChatPicture(@PathVariable Long id){
        return ResponseEntity.ok(new OperationResultDTO(chatService.deleteChatPicture(id), null));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<List<UserInfoDTO>> getChatUsers(@PathVariable Long id){
        return ResponseEntity.ok(chatService.getChatUsers(id));
    }

    @PatchMapping("/users/{id}/add")
    public ResponseEntity<OperationResultDTO> addChatUser(@RequestBody StringDTO username, @PathVariable Long id){
        return ResponseEntity.ok(new OperationResultDTO(chatService.addChatUser(username, id), null));
    }

    @PatchMapping("/users/{id}/delete")
    public ResponseEntity<OperationResultDTO> deleteChatUser(@RequestBody StringDTO username, @PathVariable Long id){
        return ResponseEntity.ok(new OperationResultDTO(chatService.deleteChatUser(username, id), null));
    }

    @PatchMapping("/users/{id}/owner")
    public ResponseEntity<OperationResultDTO> setChatOwner(@RequestBody StringDTO username, @PathVariable Long id){
        return ResponseEntity.ok(new OperationResultDTO(chatService.setChatOwner(username, id), null));
    }

    @PostMapping("/create")
    public ResponseEntity<OperationResultDTO> createChat(@RequestBody StringDTO name){
        return ResponseEntity.status(HttpStatus.CREATED).body(new OperationResultDTO(chatService.createChat(name), null));
    }

    @PostMapping("/create/{username}")
    public ResponseEntity<ChatDTO> createChat(@PathVariable String username){
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createPrivateChat(username));
    }
}
