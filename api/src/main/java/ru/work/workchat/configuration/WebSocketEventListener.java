package ru.work.workchat.configuration;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.work.workchat.model.dto.WebSocketMessageDTO;
import ru.work.workchat.model.entity.User;
import ru.work.workchat.repository.ChatRepository;
import ru.work.workchat.repository.UserRepository;

import java.util.List;

@AllArgsConstructor
@Component
public class WebSocketEventListener {

    SimpUserRegistry simpUserRegistry;
    SimpMessagingTemplate messagingTemplate;
    UserRepository userRepository;
    ChatRepository chatRepository;

    private long getUserSessions(String username) {
        SimpUser user = simpUserRegistry.getUser(username);
        if (user == null) return 0;

        return user.getSessions().size();
    }

    private void sendOnlineStatusMessage(String username, String message) {
        List<String> users = chatRepository.findByChatUsers_User_Username(username)
                .stream().flatMap(chat -> chat.getChatUsers().stream())
                .map(chatUser -> chatUser.getUser().getUsername()).distinct().toList();

        for (String user : users){
            messagingTemplate.convertAndSendToUser(
                    user,
                    "/queue/messages",
                    new WebSocketMessageDTO(message, null, username)
            );
        }
    }

    @EventListener
    @Transactional
    public void handleSessionConnected(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        User user = userRepository.findByUsername(accessor.getUser().getName()).get();
        user.setIsOnline(true);
        userRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendOnlineStatusMessage(user.getUsername(), "User went online");
            }
        });
    }

    @EventListener
    @Transactional
    public void handleSessionDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        User user = userRepository.findByUsername(accessor.getUser().getName()).get();
        if (getUserSessions(user.getUsername()) == 0){
            user.setIsOnline(false);
            userRepository.save(user);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                sendOnlineStatusMessage(user.getUsername(), "User went offline");
            }
        });
    }
}
