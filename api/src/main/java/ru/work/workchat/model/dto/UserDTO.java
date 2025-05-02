package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private String name;
    private String username;
    private String password;

    public boolean isNameValid(){
        return !name.isEmpty() && name.length() <= 25;
    }

    public boolean isUsernameValid(){
        return !username.isEmpty() && username.length() <= 25;
    }

    public boolean isPasswordValid(){
        return !password.isEmpty() && password.length() <= 25;
    }

    public UserDTO(){
        this.name = "";
        this.username = "";
        this.password = "";
    }
}
