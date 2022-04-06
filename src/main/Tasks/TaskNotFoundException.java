package Tasks;

public class TaskNotFoundException extends RuntimeException {    // Исключение, связанное с отсутствием задачи или id

    public TaskNotFoundException(String message) {
        super(message);
    }
}
