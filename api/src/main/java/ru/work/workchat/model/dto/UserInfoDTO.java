package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.work.workchat.model.entity.User;

@AllArgsConstructor
@Getter
@Setter
public class UserInfoDTO {
    private String name;
    private String username;

    public UserInfoDTO(User user){
        this.name = user.getName();
        this.username = user.getUsername();
    }
}
