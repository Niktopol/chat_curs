package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import ru.work.workchat.model.entity.User;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class ChatsPageDTO {
    List<ChatDTO> chats;
    int page;
    boolean last;

    public ChatsPageDTO(Page<User> users){
        chats = users.getContent().stream().map(user -> new ChatDTO(user.getId(), user.getUsername(), user.getName(), true, true)).toList();
        this.page = users.getNumber();
        this.last = users.isLast();
    }
}
