package ru.work.workchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.work.workchat.model.entity.ChatUser;
import ru.work.workchat.model.entity.key.ChatUserKey;

import java.util.List;

public interface ChatUserRepository extends JpaRepository<ChatUser, ChatUserKey> {

    List<ChatUser> findByChatIdAndRole(Long chatId, ChatUser.Role role);
}
