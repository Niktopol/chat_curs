package ru.work.workchat.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.work.workchat.configuration.excepion.UserNotFoundException;
import ru.work.workchat.model.dto.OperationResultDTO;

@RestControllerAdvice
public class UserExceptionAdvice {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<OperationResultDTO> handleProfileUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OperationResultDTO(null, e.getMessage()));
    }
}
