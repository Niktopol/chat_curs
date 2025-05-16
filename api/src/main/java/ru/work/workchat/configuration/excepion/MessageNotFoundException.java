package ru.work.workchat.configuration.excepion;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException() {
        super("Not authorized to perform this action");
    }

    public MessageNotFoundException(String message){
        super(message);
    }
}
