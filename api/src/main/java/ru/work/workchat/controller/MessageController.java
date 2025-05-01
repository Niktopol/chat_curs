package ru.work.workchat.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.work.workchat.model.dto.MessageDTO;
import ru.work.workchat.service.MessageService;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/{chatId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long chatId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "100") int size){
        return null;
    }

    @PostMapping("/{chatId}")
    public ResponseEntity<String> sendMessage(@PathVariable Long chatId,
                                              @RequestParam("message") String message,
                                              @RequestParam("images") List<MultipartFile> images){
        for (MultipartFile file : images) {
            continue;
        }

        return null;
    }

    @PostMapping("/user/{username}")
    public ResponseEntity<String> sendMessageToUser(@PathVariable String username,
                                                    @RequestParam("message") String message,
                                                    @RequestParam("images") List<MultipartFile> images){
        for (MultipartFile file : images) {
            continue;
        }

        return null;
    }

    @PatchMapping("/{msgId}")
    public ResponseEntity<String> patchMessage(@PathVariable Long msgId, String message){
        return null;
    }

    @DeleteMapping("/{msgId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long msgId){
        return null;
    }
}
