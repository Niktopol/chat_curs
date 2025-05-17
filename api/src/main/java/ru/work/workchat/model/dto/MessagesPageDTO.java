package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import ru.work.workchat.model.entity.Message;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class MessagesPageDTO {
    List<MessageDTO> messages;
    int page;
    boolean last;

    public MessagesPageDTO(Page<Message> messages){
        this.messages = messages.getContent().stream().map(MessageDTO::new).toList();
        this.page = messages.getNumber();
        this.last = messages.isLast();
    }
}
