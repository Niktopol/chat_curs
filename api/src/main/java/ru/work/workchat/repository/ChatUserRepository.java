package ru.work.workchat.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.work.workchat.model.entity.ChatUser;
import ru.work.workchat.model.entity.key.ChatUserKey;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUser, ChatUserKey> {

    List<ChatUser> findByChatIdAndRole(Long chatId, ChatUser.Role role);
    @EntityGraph(attributePaths = {"user"})
    List<ChatUser> findByChatIdOrderByUser_Name(Long chatId);
    Optional<ChatUser> findByChatIdAndUser_Username(Long chatId, String username);
}
