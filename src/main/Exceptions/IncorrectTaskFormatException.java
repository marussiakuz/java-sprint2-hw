package Exceptions;

import java.io.IOException;

public class IncorrectTaskFormatException extends IOException {
    public IncorrectTaskFormatException(String message) {
        super(message);
    }
}
