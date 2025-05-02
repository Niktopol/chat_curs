package ru.work.workchat.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.work.workchat.model.dto.UserDTO;
import ru.work.workchat.service.AuthService;

@AllArgsConstructor
@RestController
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userData){
        userData.setName(userData.getName().strip());
        userData.setUsername(userData.getUsername().strip());
        userData.setPassword(userData.getPassword().strip());
        String resp = authService.register(userData);
        if(resp.isEmpty()){
            return ResponseEntity.status(HttpServletResponse.SC_CREATED).body("Account created");
        }else{
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(resp);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userData, HttpServletRequest request, HttpServletResponse response){
        String resp = authService.login(userData, request, response);
        if(resp.isEmpty()){
            return ResponseEntity.ok("Logged in successfully");
        }else{
            return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).body(resp);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<String> profile(){
        return ResponseEntity.ok(authService.profile());
    }
}
