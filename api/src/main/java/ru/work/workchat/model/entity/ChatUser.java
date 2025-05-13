package ru.work.workchat.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.work.workchat.model.entity.key.ChatUserKey;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chat_member")
public class ChatUser {
    public enum Role{
        MEMBER,
        OWNER
    }

    @EmbeddedId
    private ChatUserKey id;

    @ManyToOne
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public ChatUser(Chat chat, User user, Role role){
        this.chat = chat;
        this.user = user;
        this.id = new ChatUserKey(chat.getId(), user.getId());
        this.role = role;
    }
}
