package ru.work.workchat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.work.workchat.model.dto.OperationResultDTO;
import ru.work.workchat.model.dto.UserDTO;
import ru.work.workchat.model.dto.UserInfoDTO;
import ru.work.workchat.service.AuthService;

@AllArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<OperationResultDTO> register(@RequestBody UserDTO userData){
        String resp = authService.register(userData);
        if(resp.isEmpty()){
            return ResponseEntity.status(HttpServletResponse.SC_CREATED).body(new OperationResultDTO("Акаунт создан", null));
        }else{
            if (resp.equals("Произошла ошибка регистрации")){
                return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).body(new OperationResultDTO(null, resp));
            } else {
                return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(new OperationResultDTO(null, resp));
            }
        }
    }

    @PostMapping("/login")
    public ResponseEntity<OperationResultDTO> login(@RequestBody UserDTO userData, HttpServletRequest request, HttpServletResponse response){
        String resp = authService.login(userData, request, response);
        if(resp.isEmpty()){
            return ResponseEntity.ok(new OperationResultDTO("Вход выполнен", null));
        }else{
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(new OperationResultDTO(null, resp));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserInfoDTO> profile(){
        return ResponseEntity.ok(authService.profile());
    }
}
