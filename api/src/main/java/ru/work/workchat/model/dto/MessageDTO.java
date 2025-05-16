package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.work.workchat.model.entity.Message;

@AllArgsConstructor
@Getter
@Setter
public class MessageDTO {
    private Long id;
    private String text;
    private boolean isAttachment;
    private String sender;

    public MessageDTO(Message message){
        this.id = message.getId();
        this.text = message.getText();
        this.isAttachment = message.isAttachment();
        this.sender = message.getSender().getUsername();
    }
}
