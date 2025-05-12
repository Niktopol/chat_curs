package ru.work.workchat.configuration.excepion;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException() {
        super("Chat not found");
    }

    public ChatNotFoundException(String message){
        super(message);
    }
}
