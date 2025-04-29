package ru.work.workchat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.work.workchat.model.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
