package Exceptions;

public class TimeIntersectionException extends RuntimeException {    // исключение в случае пересечения по времени

    public TimeIntersectionException(String message) {
        super(message);
    }
}
