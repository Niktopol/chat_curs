package ru.work.workchat.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.work.workchat.model.dto.OperationResultDTO;

@RestControllerAdvice
public class GeneralExceptionAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperationResultDTO> invalidArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new OperationResultDTO(null, e.getMessage()));
    }
}
