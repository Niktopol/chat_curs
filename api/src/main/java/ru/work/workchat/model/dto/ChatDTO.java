package ru.work.workchat.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatDTO {
    private Long id;
    private String username;
    private String name;
    private boolean isPrivate;
    private boolean isNew;
}
