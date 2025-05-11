package ru.work.workchat.configuration.excepion;

public class ImageNotFoundException extends RuntimeException {

    public ImageNotFoundException() {
        super("Image not found");
    }

    public ImageNotFoundException(String message){
        super(message);
    }
}
