package ru.work.workchat.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import ru.work.workchat.model.dto.UserDTO;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.UserRepository;

@AllArgsConstructor
@Service
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final SecurityContextRepository securityContextRepository;
    private final AuthenticationManager authenticationManager;
    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Transactional
    public String createUser(User user) {
        try{
            if (userRepository.findByUsername(user.getUsername()).isPresent()){
                return String.format("Имя пользователя '%s' уже занято", user.getUsername());
            }
            userRepository.save(user);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return "Произошла ошибка регистрации";
        }
        return "";
    }

    @Transactional
    public String register(UserDTO userData) {
        if (!userData.isNameValid()){
            return "Неверный никнейм";
        }
        if (!userData.isUsernameValid()){
            return "Неверное имя пользователя";
        }
        if (!userData.isPasswordValid()){
            return "Неверный пароль";
        }
        User user = new User(userData.getName(), userData.getUsername(),
                passwordEncoder.encode(userData.getPassword()), User.Role.USER);

        return createUser(user);
    }

    public String login(UserDTO userData, HttpServletRequest request, HttpServletResponse response) {
        if (!userData.isUsernameValid()){
            return "Неверное имя пользователя";
        }
        if (!userData.isPasswordValid()){
            return "Неверный пароль";
        }
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                userData.getUsername(), userData.getPassword());
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(token);
        }catch (AuthenticationException e){
            return "Неверное имя пользователя или пароль";
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextRepository.saveContext(context, request, response);

        return "";
    }
}
