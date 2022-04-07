package Enums;

public enum StatusOfTask {    // перечисление возможных статусов
    NEW("NEW"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE");

    private final String status;

    private StatusOfTask(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
