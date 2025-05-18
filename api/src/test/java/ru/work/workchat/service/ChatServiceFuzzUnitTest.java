package ru.work.workchat.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.work.workchat.model.dto.StringDTO;
import ru.work.workchat.model.entity.Chat;
import ru.work.workchat.model.entity.ChatUser;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.ChatRepository;
import ru.work.workchat.repository.ChatUserRepository;
import ru.work.workchat.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.work.workchat.model.entity.ChatUser.Role.OWNER;

@SpringBootTest
@Transactional
public class ChatServiceFuzzUnitTest {

    private ChatService chatService;
    private ChatRepository chatRepository;
    private UserRepository userRepository;
    private ChatUserRepository chatUserRepository;

    private final Long chatId = 1L;

    private List<String> generateFuzzInputs() {
        return List.of(
                "",
                " ",
                "a",
                "a".repeat(100),
                "   spaced name   ",
                "🔥🔥🔥",
                "'; DROP TABLE chats; --",
                "<script>alert(1)</script>"
        );
    }

    @BeforeEach
    void setUp() {
        chatRepository = mock(ChatRepository.class);
        userRepository = mock(UserRepository.class);
        chatUserRepository = mock(ChatUserRepository.class);

        // Поведение для save()
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Пользователь
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("fuzz_user");

        when(userRepository.findByUsername("fuzz_user")).thenReturn(Optional.of(mockUser));

        // Настройка сервиса
        chatService = new ChatService(userRepository, chatRepository, chatUserRepository, null);

        // SecurityContext
        var auth = new UsernamePasswordAuthenticationToken("fuzz_user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void fuzzCreateChatName() {
        List<String> inputs = generateFuzzInputs();

        for (String input : inputs) {
            try {
                StringDTO dto = new StringDTO(input);
                String result = chatService.createChat(dto);
                System.out.println("✅ Passed: \"" + input + "\" -> " + result);
            } catch (IllegalArgumentException ex) {
                System.out.println("⚠️ Rejected: \"" + input + "\" -> " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("❌ ERROR: \"" + input + "\" -> " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                fail("Unexpected exception for input: " + input);
            }
        }
    }

    @Test
    void fuzzEditChatName() {
        User mockUser = userRepository.findByUsername("fuzz_user").get();

        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setName("Old");

        ChatUser ownerChatUser = new ChatUser();
        ownerChatUser.setUser(mockUser);

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(chatUserRepository.findByChatIdAndRole(chatId, OWNER))
                .thenReturn(List.of(ownerChatUser));
        when(userRepository.findByUsername("fuzz_user"))
                .thenReturn(Optional.of(mockUser));

        List<String> inputs = generateFuzzInputs();

        for (String input : inputs) {
            try {
                StringDTO dto = new StringDTO(input);
                String result = chatService.editChatName(dto, chatId);
                System.out.println("✅ Edit Passed: \"" + input + "\" -> " + result);
            } catch (IllegalArgumentException ex) {
                System.out.println("⚠️ Edit Rejected: \"" + input + "\" -> " + ex.getMessage());
            } catch (Exception ex) {
                System.err.println("❌ Edit ERROR: \"" + input + "\" -> " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                fail("Unexpected exception for edit input: " + input);
            }
        }
    }
}
