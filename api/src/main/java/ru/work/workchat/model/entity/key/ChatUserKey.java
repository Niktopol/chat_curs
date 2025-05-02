package ru.work.workchat.model.entity.key;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ChatUserKey implements Serializable {
    private Long chatId;
    private Long userId;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChatUserKey)) return false;
        return Objects.equals(this.chatId, ((ChatUserKey) obj).chatId) && Objects.equals(userId, ((ChatUserKey) obj).userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.chatId, this.userId);
    }
}
