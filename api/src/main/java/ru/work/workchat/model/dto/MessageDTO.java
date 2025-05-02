package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MessageDTO {
    @AllArgsConstructor
    @Getter
    @Setter
    public static class MessageSenderDTO{
        private String senderName;
        private String senderUsername;
    }
    private Long id;
    private String text;
    private boolean isAttachment;
    private MessageSenderDTO sender;
}
