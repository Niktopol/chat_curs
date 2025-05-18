package ru.work.workchat.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import ru.work.workchat.model.dto.UserDTO;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class AuthServiceFuzzUnitTest {

    private AuthService authService;
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        SecurityContextRepository securityContextRepository = mock(SecurityContextRepository.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        FindByIndexNameSessionRepository<? extends Session> sessionRepository = mock(FindByIndexNameSessionRepository.class);

        authService = new AuthService(passwordEncoder, userRepository, securityContextRepository, authenticationManager, sessionRepository);
    }

    @Test
    void fuzzRegister() {
        List<UserDTO> fuzzInputs = List.of(
                new UserDTO(null, "validUser", "validPass"),
                new UserDTO("validName", "", "validPass"),
                new UserDTO("validName", "validUser", ""),
                new UserDTO("🔥🔥", "user", "pass"),
                new UserDTO("name", "user<script>", "pass"),
                new UserDTO("name", "user", "123"),
                new UserDTO("a".repeat(100), "user", "pass"),
                new UserDTO("valid", "existingUser", "validPass"),
                new UserDTO("valid", "validUser", "validPass") // valid case
        );

        when(userRepository.findByUsername("existingUser"))
                .thenReturn(Optional.of(new User()));

        when(userRepository.findByUsername("validUser"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");

        for (UserDTO dto : fuzzInputs) {
            String result = authService.register(dto);

            System.out.printf("Input: name='%s', username='%s', password='%s' -> %s%n",
                    dto.getName(), dto.getUsername(), dto.getPassword(), result);
        }

        verify(userRepository, atLeast(1)).save(any());
    }
}
