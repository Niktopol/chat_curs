package ru.work.workchat.controller;

import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.work.workchat.model.dto.ImageFileDTO;
import ru.work.workchat.model.dto.OperationResultDTO;
import ru.work.workchat.model.dto.StringDTO;
import ru.work.workchat.service.ChatService;

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

    @PostMapping("/create")
    public ResponseEntity<OperationResultDTO> createChat(@RequestBody StringDTO name){
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createChat(name.getValue()));
    }
}
