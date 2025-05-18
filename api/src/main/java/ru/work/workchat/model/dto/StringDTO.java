package ru.work.workchat.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

@AllArgsConstructor
@Getter
@Setter
public class StringDTO {
    String value;

    public boolean doMatch(String regex, int minlength, int maxLength){
        return value != null && Pattern.matches(regex, value) && value.length() >= minlength && value.length() <= maxLength;
    }
}
