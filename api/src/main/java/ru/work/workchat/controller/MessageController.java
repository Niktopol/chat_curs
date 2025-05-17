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
import ru.work.workchat.model.dto.MessageDTO;
import ru.work.workchat.model.dto.OperationResultDTO;
import ru.work.workchat.model.dto.StringDTO;
import ru.work.workchat.service.MessageService;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/{chatId}")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long chatId,
                                                        @RequestParam(defaultValue = "0") int page){
        return ResponseEntity.ok(messageService.getMessages(chatId, page));
    }

    @GetMapping("/last/{chatId}")
    public ResponseEntity<MessageDTO> getLastMessage(@PathVariable Long chatId){
        return ResponseEntity.ok(messageService.getLastMessage(chatId));
    }

    @GetMapping("/image/{msgId}")
    public ResponseEntity<ByteArrayResource> getImage(@PathVariable Long msgId){
        ImageFileDTO image = messageService.getMsgImage(msgId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + image.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .contentLength(image.getData().length)
                .body(new ByteArrayResource(image.getData()));
    }

    @PostMapping("/{chatId}")
    public ResponseEntity<OperationResultDTO> sendMessage(@PathVariable Long chatId, @RequestBody StringDTO message){
        return ResponseEntity.ok(new OperationResultDTO(messageService.sendMessage(chatId, message), null));
    }

    @PostMapping("/image/{chatId}")
    public ResponseEntity<OperationResultDTO> sendImage(@PathVariable Long chatId, @RequestParam("file") MultipartFile file){

        try {
            return ResponseEntity.ok(new OperationResultDTO(messageService.sendImage(chatId, file), null));
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new OperationResultDTO(null, "Ошибка проверки изображения"));
        }
    }
}
