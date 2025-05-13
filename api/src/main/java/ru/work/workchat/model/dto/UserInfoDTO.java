package ru.work.workchat.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.work.workchat.model.entity.User;

@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfoDTO {
    private String name;
    private String username;
    private Boolean isOnline;

    public UserInfoDTO(User user){
        this.name = user.getName();
        this.username = user.getUsername();
    }
}
