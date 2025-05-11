package ru.work.workchat.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.work.workchat.configuration.excepion.ImageNotFoundException;
import ru.work.workchat.model.dto.OperationResultDTO;

@RestControllerAdvice
public class FileExceptionAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<OperationResultDTO> handleMaxSizeException(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new OperationResultDTO(null, "Файл должен быть меньше 5 MB"));
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<OperationResultDTO> handleProfilePicNotFound(ImageNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new OperationResultDTO(null, e.getMessage()));
    }
}
