package ru.work.workchat.configuration.excepion;

public class NoAuthorityException extends RuntimeException {

    public NoAuthorityException() {
        super("Not authorized to perform this action");
    }

    public NoAuthorityException(String message){
        super(message);
    }
}
