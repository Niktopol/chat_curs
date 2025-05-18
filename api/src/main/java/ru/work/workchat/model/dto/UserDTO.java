package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private String name;
    private String username;
    private String password;

    public boolean isNameValid(){
        return name != null && Pattern.matches("^\\S+$", name) && !name.isEmpty() && name.length() <= 20;
    }

    public boolean isUsernameValid(){
        return username != null && Pattern.matches("^[A-Za-z0-9\\-_]+$", username) && !username.isEmpty() && username.length() <= 20;
    }

    public boolean isPasswordValid(){
        return password != null && Pattern.matches("^[A-Za-z0-9\\-=_#+&$]+$", password) && password.length() >= 8 && password.length() <= 25;
    }

    public UserDTO(){
        this.name = "";
        this.username = "";
        this.password = "";
    }
}
