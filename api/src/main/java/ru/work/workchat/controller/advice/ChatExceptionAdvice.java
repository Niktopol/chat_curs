package ru.work.workchat.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.work.workchat.configuration.excepion.ChatNotFoundException;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.configuration.excepion.MessageNotFoundException;
import ru.work.workchat.configuration.excepion.NoAuthorityException;
import ru.work.workchat.model.dto.OperationResultDTO;

@RestControllerAdvice
public class ChatExceptionAdvice {

    @ExceptionHandler(NoAuthorityException.class)
    public ResponseEntity<OperationResultDTO> handleNotAllowedToEditChat(NoAuthorityException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new OperationResultDTO(null, e.getMessage()));
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<OperationResultDTO> handleChatNotFound(ChatNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OperationResultDTO(null, e.getMessage()));
    }

    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<OperationResultDTO> handleMessageNotFound(MessageNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OperationResultDTO(null, e.getMessage()));
    }
}
