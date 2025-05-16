package ru.work.workchat.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.work.workchat.model.entity.Chat;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    @EntityGraph(attributePaths = {"chatUsers", "chatUsers.user"})
    List<Chat> findByIsPrivateTrueAndChatUsers_User_UsernameOrderByLastMessageTimeDesc(String username);
    @EntityGraph(attributePaths = {"chatUsers", "chatUsers.user"})
    List<Chat> findByIsPrivateTrueAndChatUsers_User_Username(String username);
    @EntityGraph(attributePaths = {"chatUsers", "chatUsers.user"})
    List<Chat> findByIsPrivateFalseAndChatUsers_User_UsernameOrderByLastMessageTimeDesc(String username);
    @EntityGraph(attributePaths = {"chatUsers", "chatUsers.user"})
    List<Chat> findByChatUsers_User_Username(String username);
}
