package ru.work.workchat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.work.workchat.model.entity.Chat;
import ru.work.workchat.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE " +
            "u.id NOT IN (SELECT cu.id.userId FROM ChatUser cu WHERE cu.chat IN :chats) " +
            "AND u.username <> :excludedUsername " +
            "AND LOWER(u.name) LIKE LOWER(CONCAT('%', :namePart, '%'))")
    Page<User> findUsersByEmptyChatsAndName(
            @Param("chats") List<Chat> chats,
            @Param("excludedUsername") String excludedUsername,
            @Param("namePart") String namePart,
            Pageable pageable
    );
    @Query("SELECT u FROM User u WHERE " +
            "u.id NOT IN (SELECT cu.id.userId FROM ChatUser cu WHERE cu.chat IN :chats) " +
            "AND u.username <> :excludedUsername " +
            "AND LOWER(u.username) LIKE LOWER(CONCAT(:usernamePrefix, '%'))")
    Page<User> findUsersByEmptyChatsAndUsername(
            @Param("chats") List<Chat> chats,
            @Param("excludedUsername") String excludedUsername,
            @Param("usernamePrefix") String usernamePrefix,
            Pageable pageable
    );
}
